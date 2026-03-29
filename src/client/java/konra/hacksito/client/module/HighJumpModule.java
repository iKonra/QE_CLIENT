package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class HighJumpModule extends Module {

    private float height = 6.0f;
    private boolean prevJump = false;
    private long lastJumpTime = 0; // timestamp del ultimo salto apretado

    public HighJumpModule() {
        super("HighJump");
    }

    public void setHeight(float h) {
        this.height = Math.max(0, h);
    }

    public float getHeight() {
        return height;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onTick() {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean nowJump = client.options.jumpKey.isPressed();
        if (nowJump && !prevJump) {
            long now = System.currentTimeMillis();

            // salto normal en tierra (siempre aplica estando en el suelo)
            if (client.player.isOnGround()) {
                Vec3d vel = client.player.getVelocity();
                float boost = 0.42f + height * 0.1f;
                client.player.setVelocity(vel.x, boost, vel.z);
            }
            // doble salto: apretar otra vez en <0.3s estando en el aire
            else if (now - lastJumpTime <= 300) {
                Vec3d vel = client.player.getVelocity();
                float boost = 0.42f + height * 0.1f;
                client.player.setVelocity(vel.x, boost, vel.z);
            }

            lastJumpTime = now;
        }
        prevJump = nowJump;
    }
}