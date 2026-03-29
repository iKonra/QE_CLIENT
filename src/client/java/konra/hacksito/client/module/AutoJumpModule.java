package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class AutoJumpModule extends Module {

    public AutoJumpModule() {
        super("AutoJump");
    }

    @Override
    public void onEnable() {
        // nada pa iniciar
    }

    @Override
    public void onDisable() {
        // nada pa limpiar
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null) return;

        if (player.isOnGround()) {
            // salto si hay movimiento horizontal (evita tocar campos de input)
            Vec3d vel = player.getVelocity();
            if (vel.x != 0 || vel.z != 0) {
                player.jump();
            }
        }
    }
}