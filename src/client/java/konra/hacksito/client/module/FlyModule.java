package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

public class FlyModule extends Module {

    private int tickCounter = 0;

    public FlyModule() {
        super("Fly");
    }

    @Override
    public void onEnable() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.setVelocity(0, 0, 0);
        }
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        // 1. EL FIX: Si el módulo no está activo, no hagas nada.
        if (!this.isEnabled()) return;

        var player = MinecraftClient.getInstance().player;
        var options = MinecraftClient.getInstance().options;
        if (player == null) return;

        // Reset de caída
        player.fallDistance = 0;

        Vec3d velocity = player.getVelocity();
        double flyY = 0;

        // 2. CONTROL VERTICAL
        if (options.jumpKey.isPressed()) {
            flyY = 0.4; 
        } else if (options.sneakKey.isPressed()) {
            flyY = -0.4;
        } else {
            // Anti-kick: pequeña fluctuación para simular movimiento
            tickCounter++;
            if (tickCounter >= 20) {
                flyY = -0.04; // Cae un poquito
                tickCounter = 0;
            } else {
                flyY = 0.01; // Sube un poquito (mantiene altura)
            }
        }

        // 3. MOVIMIENTO HORIZONTAL
        // Si no estás tocando las teclas de movimiento, frenamos la inercia
        // para que no te deslices como en hielo.
        double moveX = velocity.x;
        double moveZ = velocity.z;
        
        if (player.forwardSpeed == 0 && player.sidewaysSpeed == 0) {
            moveX = 0;
            moveZ = 0;
        }

        player.setVelocity(moveX, flyY, moveZ);
    }

    @Override
    public void onDisable() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            // Al apagar, devolvemos una velocidad de caída mínima para evitar bugs
            player.setVelocity(0, -0.01, 0);
        }
    }
}