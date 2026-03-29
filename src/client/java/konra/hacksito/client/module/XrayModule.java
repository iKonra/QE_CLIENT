package konra.hacksito.client.module;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.HashSet;
import java.util.Set;

public class XrayModule extends Module {
    /**
     * Si es true mandamos el mensajito amarillo al prender el modulo; los
     * usuarios lo pueden cambiar en la pantalla de config.
     */
    public static boolean showEnableHint = true;

    public static final Set<Block> XRAY_BLOCKS = Set.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.ANCIENT_DEBRIS, Blocks.NETHER_QUARTZ_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST
    );

    private final MinecraftClient mc = MinecraftClient.getInstance();

    // distancia maxima desde el player que escaneamos por minerales; un radio
    // mas bajo baja el trabajo del hilo y la cantidad de cajas dibujadas. se
    // puede cambiar en la UI y por defecto son 64 bloques.
    public static int SCAN_RADIUS = 64;

    // si los items tirados tambien se resaltan; se configura en la UI
    public static boolean showItems = false;

    // si solo mostramos ores que tengan al menos una cara al aire libre
    public static boolean onlyExposed = false;

    // opacidad del cubo rellenado por cada ore (0 = nada, 1 = solido)
    public static float opacity = 0f;

    // si los ores tienen un brillo relleno real alrededor (no solo lineas)
    public static boolean showGlow = false;

    // resultados del ultimo escaneo; el renderer usa estas colecciones
    // los ores estan en lista pa controlar el orden de dibujo. igual
    // desduplicamos durante el escaneo con un set temporal, pero la lista
    // publica se ordena por distancia al player (mas cerca primero) lo cual
    // mejora cuando hay un limite de dibujo.
    // Reemplaza tu actual foundOres por esto:
    public static final java.util.List<BlockPos> foundOres = new java.util.concurrent.CopyOnWriteArrayList<>();
    public static final Set<BlockPos> foundItems = new HashSet<>();

    // future usada por tarea de escaneo en fondo; evitamos lanzar dos
    // escaneos a la vez pa que el mundo no se itere paralelo.
    private java.util.concurrent.CompletableFuture<?> scanFuture = null;

    // ultima pos conocida del player cuando escaneo; rescan solo si el
    // player se movio bastante (ej 50 bloques)
    private BlockPos lastScanPos = null;
    // copia de posiciones de ores del escaneo anterior, se usa pa detectar
    // cuando se pica uno y así forzar un nuevo escaneo
    private final Set<BlockPos> lastFoundOres = new HashSet<>();

    // llevar si activamos fullbright al prender modulo pa poder volver al
    // estado anterior al apagar xray.
    private boolean autoFullBright = false;

    public XrayModule() {
        super("Xray");
    }

    /**
     * Helper usado por los mixins para saber si el player tiene el modulo
     * activado. se renombró para evitar el conflicto con el metodo de
     * instancia de `Module`.
     */
    public static boolean isXrayEnabled() {
        Module m = ModuleManager.getModule("Xray");
        return m != null && m.isEnabled();
    }

    // utilidad para testear si una posicion de bloque tiene algun vecino
    // no opaco
    private static boolean isExposed(MinecraftClient client, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!client.world.getBlockState(pos.offset(dir)).isOpaque()) {
                return true;
            }
        }
        return false;
    }

   @Override
    public void onEnable() {
        // 1. Auto-activar FullBright (Tu lógica actual)
        Module fb = ModuleManager.getModule("FullBright");
        if (fb != null && !fb.isEnabled()) {
            fb.toggle();
            autoFullBright = true;
        }

        // 2. Notificar al usuario
        if (showEnableHint && mc.player != null) {
            mc.player.sendMessage(net.minecraft.text.Text.literal("§eXray activado."), false);
        }

        // ¡IMPORTANTE! recargar el renderer para que el mixin de Xray se aplique a los bloques que ya estas viendo; sin esto, solo los bloques que aparecen por primera vez (ej. al mover la camara) se verian afectados por el mixin, lo cual es medio raro porque la piedra seguiria siendo opaca hasta que te acerques o muevas la camara.
        // Sin esto, el Mixin no se aplica a los bloques que ya estas viendo.
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload(); 
        }

        // 4. Iniciar escaneo
        lastScanPos = null;
        lastFoundOres.clear();
        triggerScan();
    }

    @Override
    public void onDisable() {
        foundOres.clear();
        lastFoundOres.clear();
        lastScanPos = null;

        // 1. Recargar el renderer para que la piedra vuelva a ser opaca
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }

        // 2. Apagar FullBright si lo prendimos nosotros
        if (autoFullBright) {
            Module fb = ModuleManager.getModule("FullBright");
            if (fb != null && fb.isEnabled()) {
                fb.toggle();
            }
            autoFullBright = false;
        }
    }

    

    @Override
    public void onTick() {
        if (!enabled) return;
        if (mc.player == null || mc.world == null) return;

        boolean shouldScan = false;
        BlockPos currentPos = mc.player.getBlockPos();

        // inicial o movio mas de 50 bloques (distancia^2 >= 2500)
        if (lastScanPos == null) {
            shouldScan = true;
        } else {
            double dx = currentPos.getX() - lastScanPos.getX();
            double dy = currentPos.getY() - lastScanPos.getY();
            double dz = currentPos.getZ() - lastScanPos.getZ();
            if (dx*dx + dy*dy + dz*dz >= 2500) {
                shouldScan = true;
            }
        }

        // detectar si algun ore antes encontrado desaparecio (picado)
        if (!shouldScan && !lastFoundOres.isEmpty()) {
            for (BlockPos pos : lastFoundOres) {
                var state = mc.world.getBlockState(pos);
                if (!XRAY_BLOCKS.contains(state.getBlock())) {
                    shouldScan = true;
                    break;
                }
            }
        }

        if (shouldScan) {
            triggerScan();
        }
    }

    // Lanza un escaneo (antes era asincrono, provocaba lecturas de mundo en
    // otro hilo y resultados erráticos). ahora se ejecuta directamente en el
    // hilo del cliente; el coste es asumible porque el radio por defecto es 64.
    private void triggerScan() {
        // si ya hay un scan en curso no hacemos nada; el valor de scanFuture ya no
        // es realmente utilizado, pero lo dejamos por compatibilidad con el
        // resto del código.
        if (scanFuture != null && !scanFuture.isDone()) {
            return; // ya se esta escaneando
        }
        // marca un "future" completado inmediatamente para que otras llamadas no
        // vuelvan a lanzar otro escaneo instantáneamente.
        scanFuture = java.util.concurrent.CompletableFuture.completedFuture(null);

        var client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        BlockPos playerPos = client.player.getBlockPos();
        int radius = SCAN_RADIUS; // usar el radio configurado actual
        Set<BlockPos> results = new HashSet<>();
        Set<BlockPos> itemResults = new HashSet<>();

        for (BlockPos pos : BlockPos.iterate(
                playerPos.add(-radius, -radius, -radius),
                playerPos.add(radius, radius, radius))) {
            var state = client.world.getBlockState(pos);
            if (XRAY_BLOCKS.contains(state.getBlock())) {
                if (!onlyExposed || isExposed(client, pos)) {
                    results.add(pos.toImmutable());
                }
            }
        }

        // opcionalmente resaltar items tirados iterando entidades
        if (showItems) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof ItemEntity) {
                    BlockPos pos = entity.getBlockPos();
                    if (Math.abs(pos.getX() - playerPos.getX()) <= radius &&
                        Math.abs(pos.getY() - playerPos.getY()) <= radius &&
                        Math.abs(pos.getZ() - playerPos.getZ()) <= radius) {
                        itemResults.add(pos);
                    }
                }
            }
        }

        // antes de guardar los resultados comprobamos que el módulo sigue
        // activado; si el jugador apagó xray mientras se escaneaba no queremos
        // volver a rellenar foundOres.
        if (!enabled) {
            return;
        }

        // ordenar resultados por distancia^2 al player para que los
        // ores mas cercanos se dibujen primero (hace el limite de dibujo
        // mas util).
        java.util.List<BlockPos> sorted = new java.util.ArrayList<>(results);
        sorted.sort(java.util.Comparator.comparingDouble(pos -> pos.getSquaredDistance(playerPos)));

        foundOres.clear();
        foundOres.addAll(sorted);
        foundItems.clear();
        foundItems.addAll(itemResults);
        lastScanPos = playerPos;
        lastFoundOres.clear();
        lastFoundOres.addAll(results);
        // sin feedback en chat o consola (user prefiere operacion silenciosa)
    }

    /**
     * Wrapper publico para que llamadores externos (ej. listeners) puedan
     * forzar un escaneo.
     */
    public void forceScan() {
        triggerScan();
    }
}
