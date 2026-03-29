package konra.hacksito.client.mixin;

import konra.hacksito.client.module.ModuleManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    
    // Este método maneja el empuje físico entre entidades (Collision)
    @Inject(method = "pushAwayFrom", at = @At("HEAD"), cancellable = true)
    private void onPushAwayFrom(Entity entity, CallbackInfo ci) {
        var mod = ModuleManager.getModule("NoPush");
        if (mod != null && mod.isEnabled()) {
            // Si el que está siendo empujado es el jugador, cancelamos
            // (Nota: MinecraftClient.getInstance().player == (Object)this)
            ci.cancel();
        }
    }
}