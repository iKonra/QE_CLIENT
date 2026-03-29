package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityPose;

public class SwimmingModule extends Module {

    public SwimmingModule() {
        super("Swimming");
    }

    @Override
    public void onEnable() {
        // No hace falta lógica pesada, el Mixin se encarga
    }

    @Override
    public void onTick() {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;

        // Forzamos el estado de nado para que el motor de físicas nos deje entrar en 1x1
        player.setSwimming(true);
    }

    @Override
    public void onDisable() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.setSwimming(false);
            player.setPose(EntityPose.STANDING);
            // Esto fuerza a Minecraft a refrescar la hitbox inmediatamente
            player.calculateDimensions(); 
        }
    }
}