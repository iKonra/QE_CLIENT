package konra.hacksito.client.module;

import konra.hacksito.client.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class MobESPModule extends Module {
    // Usamos una sola lista y la limpiamos para evitar crear objetos nuevos constantemente
    private final List<LivingEntity> mobs = new ArrayList<>();
    
    private PlayerESPModule.EspStyle style = PlayerESPModule.EspStyle.BOTH;
    private boolean filterInvisible = false;
    private boolean filterPassive = false;

    public MobESPModule() {
        super("MobESP");
    }
    @Override
    public void onEnable() {
    }
    @Override
    public void onDisable() {
        synchronized(mobs) { mobs.clear(); }
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        // Limpiamos y llenamos la lista directamente (evita crear ArrayLists temporales)
        synchronized(mobs) {
            mobs.clear();
            for (Entity e : client.world.getEntities()) {
                if (!(e instanceof LivingEntity ent) || ent == client.player) continue;
                if (ent instanceof net.minecraft.entity.player.PlayerEntity) continue;
                if (ent.isRemoved() || !ent.isAlive()) continue;
                
                if (filterInvisible && ent.isInvisible()) continue;
                if (filterPassive && ent instanceof PassiveEntity) continue;

                mobs.add(ent);
            }
        }
    }

    public void render(MatrixStack matrices, double camX, double camY, double camZ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || mobs.isEmpty()) return;

        // tickDelta es necesario para que el movimiento de las cajas sea suave (interpolación)
        float tickDelta = client.getTickDelta();
        Vec3d camPos = new Vec3d(camX, camY, camZ);

        synchronized(mobs) {
            for (LivingEntity ent : mobs) {
                // INTERPOLACIÓN: Calcula la posición exacta entre el tick anterior y el actual
                double x = MathHelper.lerp(tickDelta, ent.lastRenderX, ent.getX()) - camX;
                double y = MathHelper.lerp(tickDelta, ent.lastRenderY, ent.getY()) - camY;
                double z = MathHelper.lerp(tickDelta, ent.lastRenderZ, ent.getZ()) - camZ;

                // Ajustamos la caja al tamaño real de la entidad en esa posición interpolada
                float width = ent.getWidth() / 2f;
                float height = ent.getHeight();
                Box box = new Box(x - width, y, z - width, x + width, y + height, z + width);

                // Lógica de colores (Hostil vs Pasivo)
                float r = 1f, g = 1f, b = 1f;
                if (ent instanceof HostileEntity) {
                    r = 1f; g = 0.2f; b = 0.2f; 
                } else if (ent instanceof PassiveEntity) {
                    r = 0.2f; g = 1f; b = 0.2f;
                }

                if (style.hasBoxes()) {
                    RenderUtils.drawWireBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, 0.8f);
                }

                if (style.hasLines()) {
                    // La línea sale desde donde miramos nosotros hasta los pies del mob
                    RenderUtils.drawLine(matrices, Vec3d.ZERO, new Vec3d(x, y, z), r, g, b, 0.5f);
                }
            }
        }
    }

    // Getters y Setters
    public PlayerESPModule.EspStyle getStyle() { return style; }
    public void setStyle(PlayerESPModule.EspStyle s) { this.style = s; }
    public boolean isFilterInvisible() { return filterInvisible; }
    public void setFilterInvisible(boolean v) { this.filterInvisible = v; }
    public boolean isFilterPassive() { return filterPassive; }
    public void setFilterPassive(boolean v) { this.filterPassive = v; }
}