package konra.hacksito.client.mixin;

import konra.hacksito.client.module.InfoModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseClick(long window, int button, int action, int mods, CallbackInfo ci) {
        // action 1 es presionar. Botón 0 es click izquierdo.
        if (button == 0 && action == 1) {
            InfoModule info = (InfoModule) ModuleManager.getModule("Info");
            if (info != null && info.isEnabled()) {
                info.registerClick();
            }
        }
    }
}