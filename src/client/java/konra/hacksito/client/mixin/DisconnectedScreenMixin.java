package konra.hacksito.client.mixin;

import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {
    private int timeToReconnect = 100; // 5 segundos aprox
    private static ServerInfo lastServer;

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        ServerInfo current = MinecraftClient.getInstance().getCurrentServerEntry();
        if (current != null) lastServer = current;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var mod = ModuleManager.getModule("AutoReconnect");
        if (mod == null || !mod.isEnabled() || lastServer == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        timeToReconnect--;

        String text = "Reconectando en " + Math.max(0, timeToReconnect / 20) + "s...";
        context.drawCenteredTextWithShadow(mc.textRenderer, text, context.getScaledWindowWidth() / 2, 20, 0xFFFFFF);

        if (timeToReconnect <= 0) {
            net.minecraft.client.gui.screen.ConnectScreen.connect(
                (DisconnectedScreen)(Object)this, 
                mc, 
                ServerAddress.parse(lastServer.address), 
                lastServer,
                false
            );
        }
    }
}