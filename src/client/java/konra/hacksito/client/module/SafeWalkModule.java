package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SafeWalkModule extends Module {

    private boolean slowMode = false; // false = paro estricto, true = lento pero igual se puede caer

    public SafeWalkModule() {
        super("SafeWalk");
    }

    @Override
    public void onEnable() {
        // nada
    }

    @Override
    public void onDisable() {
        // nada
    }

    public boolean isSlowMode() {
        return slowMode;
    }

    public void setSlowMode(boolean slowMode) {
        this.slowMode = slowMode;
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null || player.getWorld() == null) return;

        if (player.isOnGround()) {
            Vec3d vel = player.getVelocity();
            if (vel.x != 0 || vel.z != 0) {
                double checkX = player.getX() + vel.x * 0.5;
                double checkZ = player.getZ() + vel.z * 0.5;
                BlockPos below = new BlockPos((int)Math.floor(checkX), (int)Math.floor(player.getY() - 1), (int)Math.floor(checkZ));
                if (player.getWorld().getBlockState(below).isAir()) {
                    if (slowMode) {
                        // frena suave, igual deja caer
                        player.setVelocity(vel.x * 0.1, vel.y, vel.z * 0.1);
                    } else {
                        // parada estricta justo en el borde como al agacharse
                        player.setVelocity(0, vel.y, 0);
                        // la vieja aproximacion ponia en cero los campos de input;
                        // desaparecieron en versiones recientes asi que solo confiamos
                        // en resetear la velocidad.
                    }
                }
            }
        }
    }
}