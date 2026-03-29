package konra.hacksito.client.mixin;

import konra.hacksito.client.module.AimAssistModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack; // Importante
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // El método real en 1.20.1 pide: float, long, MatrixStack, CallbackInfo
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        var mod = ModuleManager.getModule("AimAssist");
        if (mod instanceof AimAssistModule aim && aim.isEnabled()) {
            aim.onRender(tickDelta);
        }
    }
}