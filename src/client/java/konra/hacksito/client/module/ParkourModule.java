package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParkourModule extends Module {

    public ParkourModule() {
        super("Parkour");
    }

    @Override
    public void onEnable() {
        // nada que inicializar
    }

    @Override
    public void onDisable() {
        // nada que limpiar
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
                double offset = 0.3;
                double checkX = player.getX() + vel.x * offset;
                double checkZ = player.getZ() + vel.z * offset;
                BlockPos ahead = new BlockPos((int)Math.floor(checkX), (int)Math.floor(player.getY()), (int)Math.floor(checkZ));
                BlockPos below = ahead.down();
                // saltar si vamos a salirnos de un bloque (no hay soporte un poco adelante)
                if (player.getWorld().getBlockState(ahead).isAir() && player.getWorld().getBlockState(below).isAir()) {
                    player.jump();
                }
            }
        }
    }
}