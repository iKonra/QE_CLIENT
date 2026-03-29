package konra.hacksito.client.module;

/**
 * Disables the hurt camera effect when the player takes damage.
 * simple stub; toggling the module does nothing until logic is added.
 */
public class NoHurtCamModule extends Module {
    public NoHurtCamModule() {
        super("NoHurtCam");
    }

    @Override
    public void onEnable() {
        // nada por ahora
    }

    @Override
    public void onDisable() {
        // nada por ahora
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null && client.player.hurtTime > 0) {
            client.player.hurtTime = 0;
            client.player.maxHurtTime = 0;
        }
    }
}
