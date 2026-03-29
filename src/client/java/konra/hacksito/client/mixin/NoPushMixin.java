package konra.hacksito.client.mixin;

import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import konra.hacksito.client.module.NoPushModule;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class NoPushMixin {
    @Inject(method = "takeKnockback(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onTakeKnockback(double strength, double x, double z, CallbackInfo ci) {
        Module m = ModuleManager.getModule("NoPush");
        if (m instanceof NoPushModule mod && mod.isEnabled()) {
            ci.cancel();
        }
    }
}
