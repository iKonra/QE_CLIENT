package konra.hacksito.client.module;

import konra.hacksito.client.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PlayerESPModule extends Module {
    private final List<PlayerEntity> players = new ArrayList<>();

    private EspStyle style = EspStyle.BOTH;
    private BoxSize boxSize = BoxSize.ACCURATE;
    private boolean filterInvisible = false;
    private boolean filterSleeping = false;
    private boolean showSelfHitbox = true; // Opción para ver tu propia hitbox

    public PlayerESPModule() {
        super("PlayerESP");
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        synchronized(players) { players.clear(); }
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        synchronized(players) {
            players.clear();
            for (PlayerEntity player : client.world.getPlayers()) {
                if (player == client.player) continue;
                if (player.isRemoved() || !player.isAlive()) continue;
                if (filterInvisible && player.isInvisible()) continue;
                if (filterSleeping && player.isSleeping()) continue;
                
                players.add(player);
            }
        }
    }

    public void render(MatrixStack matrices, double camX, double camY, double camZ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        float tickDelta = client.getTickDelta();
        
        // 1. Dibujar Hitbox propia (Suave y precisa)
        if (showSelfHitbox) {
            renderSelfHitbox(matrices, client, camX, camY, camZ);
        }

        synchronized(players) {
            for (PlayerEntity other : players) {
                // Interpolación para movimiento fluido
                double x = MathHelper.lerp(tickDelta, other.lastRenderX, other.getX()) - camX;
                double y = MathHelper.lerp(tickDelta, other.lastRenderY, other.getY()) - camY;
                double z = MathHelper.lerp(tickDelta, other.lastRenderZ, other.getZ()) - camZ;

                float extra = (float) boxSize.getExtra();
                float w = (other.getWidth() / 2f) + extra;
                float h = other.getHeight() + (extra * 2);
                
                Box box = new Box(x - w, y - extra, z - w, x + w, y + h, z + w);

                // Lógica de color por distancia (Verde cerca -> Rojo lejos)
                float dist = client.player.distanceTo(other);
                float f = MathHelper.clamp(dist / 30f, 0f, 1f);
                float r = f;
                float g = 1f - f;
                float b = 0.2f;

                if (style.hasBoxes()) {
                    RenderUtils.drawFilledBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, 0.15f);
                    RenderUtils.drawWireBox(matrices, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, 0.8f);
                }

                if (style.hasLines()) {
                    // Línea desde el centro de tu vista hasta el pecho del enemigo
                    RenderUtils.drawLine(matrices, Vec3d.ZERO, new Vec3d(x, y + (h/2), z), r, g, b, 0.6f);
                }
            }
        }
    }

    private void renderSelfHitbox(MatrixStack matrices, MinecraftClient client, double cX, double cY, double cZ) {
        Box b = client.player.getBoundingBox();
        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buf = consumers.getBuffer(RenderLayer.getLines()); // RenderLayer oficial para líneas
        
        RenderUtils.writeWireBoxLines(matrices, buf, 
            b.minX - cX, b.minY - cY, b.minZ - cZ,
            b.maxX - cX, b.maxY - cY, b.maxZ - cZ,
            1f, 0f, 0f, 1f);
        consumers.draw();
    }

    // --- Enums y Boilerplate ---
    public enum EspStyle {
        BOXES(true, false), LINES(false, true), BOTH(true, true);
        private final boolean b, l;
        EspStyle(boolean b, boolean l) { this.b = b; this.l = l; }
        public boolean hasBoxes() { return b; }
        public boolean hasLines() { return l; }
    }

    public enum BoxSize {
        ACCURATE(0d), FANCY(0.1d);
        private final double e;
        BoxSize(double e) { this.e = e; }
        public double getExtra() { return e; }
    }
    
    // Getters y Setters...
}