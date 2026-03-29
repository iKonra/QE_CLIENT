package konra.hacksito.client.mixin;

import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(CallbackInfoReturnable<Float> cir) {
        var mod = ModuleManager.getModule("FullBright");
        if (mod != null && mod.isEnabled()) {
            // Retornamos 1.0f (brillo máximo) ignorando el valor real del mundo
            cir.setReturnValue(1.0f);
        }
    }
}