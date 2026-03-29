package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.block.AirBlock;
import net.minecraft.block.FluidBlock;

public class JesusModule extends Module {

    private boolean bypass = false;
    private int tickTimer = 0;

    public JesusModule() {
        super("Jesus");
    }

    public boolean isBypass() { return bypass; }
    public void setBypass(boolean value) { this.bypass = value; }


    @Override
    public void onEnable() {
        // Se ejecuta al activar el módulo
        tickTimer = 0;
    }

    @Override
    public void onDisable() {
        // Se ejecuta al desactivar el módulo
        tickTimer = 0;
    }

    @Override
    public void onTick() {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Si te agachas, te hundes a propósito (útil para lootear bajo el agua)
        if (client.options.sneakKey.isPressed()) return;

        // Lógica si estás DENTRO del líquido
        if (client.player.isTouchingWater() || client.player.isInLava()) {
            Vec3d vel = client.player.getVelocity();
            // Subida suave para llegar a la superficie
            client.player.setVelocity(vel.x, 0.12, vel.z);
            tickTimer = 0;
            return;
        }

        // Lógica si estás SOBRE la superficie
        if (isOverLiquid()) {
            Vec3d vel = client.player.getVelocity();
            
            // Bypass NCP: Alternamos micro-saltos y caídas para engañar al anticheat
            if (bypass) {
                if (tickTimer == 0) {
                    // Un salto casi invisible (0.02 es bajísimo)
                    client.player.setVelocity(vel.x, 0.02, vel.z);
                } else if (tickTimer == 1) {
                    client.player.setVelocity(vel.x, 0.01, vel.z);
                } else if (tickTimer >= 2) {
                    // Reseteamos el timer para el ciclo
                    tickTimer = -1;
                }
            } else {
                // Modo Trampolín estándar (más rápido pero menos seguro)
                if (client.player.fallDistance > 0.1) {
                    client.player.setVelocity(vel.x, 0.1, vel.z);
                }
            }

            // Evitar que el servidor crea que estamos volando enviando paquetes falsos de OnGround
            if (client.player.age % 2 == 0) {
                double x = client.player.getX();
                double y = client.player.getY() + (bypass ? 0.01 : 0);
                double z = client.player.getZ();
                
                client.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true)
                );
            }
            
            tickTimer++;
        } else {
            tickTimer = 0;
        }
    }

    public boolean isOverLiquid() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return false;

        // Caja de colisión justo debajo de los pies
        Box box = client.player.getBoundingBox().contract(0.01).offset(0, -0.1, 0);

        int minX = (int)Math.floor(box.minX);
        int maxX = (int)Math.floor(box.maxX);
        int minY = (int)Math.floor(box.minY);
        int maxY = (int)Math.floor(box.maxY);
        int minZ = (int)Math.floor(box.minZ);
        int maxZ = (int)Math.floor(box.maxZ);

        boolean foundLiquid = false;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = client.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    
                    // Si hay un bloque sólido debajo, no aplicar Jesus (evita bugs en la orilla)
                    if (!(block instanceof AirBlock) && !(block instanceof FluidBlock)) {
                        return false;
                    }
                    if (block instanceof FluidBlock) {
                        foundLiquid = true;
                    }
                }
            }
        }
        return foundLiquid;
    }
}