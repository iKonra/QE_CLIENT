package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;

public class SprintModule extends Module {

    public SprintModule() {
        super("Sprint");
    }

    @Override
    public void onEnable() {
        // nada especial
    }

    @Override
    public void onDisable() {
        // nada especial
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            // el hack de sprint mas simple; solo manten la bandera encendida
            player.setSprinting(true);
        }
    }
}