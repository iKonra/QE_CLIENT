package konra.hacksito.client.module;

import konra.hacksito.client.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public class FastPlaceModule extends Module {
    private float speed = 1.0f;

    public FastPlaceModule() {
        super("FastPlace");
    }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.interactionManager == null) return;

        // Solo actuar si el jugador está intentando usar/colocar algo
        if (!client.options.useKey.isPressed()) return;

        MinecraftClientAccessor accessor = (MinecraftClientAccessor) client;

        // LÓGICA DE VELOCIDAD DINÁMICA
        if (speed >= 4.0f) {
            // A partir de 4x, el cooldown es siempre 0 (1 bloque por tick)
            accessor.setItemUseCooldown(0);

            // Si la velocidad es mayor a 4x, forzamos clics extra manuales
            // Ejemplo: a 5x, tenemos un 50% de chance de poner 2 bloques en un solo tick
            if (speed > 4.0f && Math.random() < (speed - 4.0f)) {
                interactWrapper(client);
            }
        } else {
            // Mapeo: 1x -> 4 ticks (vanilla), 2x -> 2 ticks, 3x -> 1 tick
            int targetCooldown = Math.max(0, 4 - Math.round(speed));
            if (accessor.getItemUseCooldown() > targetCooldown) {
                accessor.setItemUseCooldown(targetCooldown);
            }
        }
    }

    // Método auxiliar para simular el click derecho de forma segura
    private void interactWrapper(MinecraftClient client) {
        // Ejecuta la acción de "usar item" sin esperar al siguiente tick
        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        client.interactionManager.interactItem(client.player, Hand.OFF_HAND);
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}
}