package konra.hacksito.client.mixin;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// IMPORTA TU PROPIO MANAGER (ajusta la ruta si es distinta)
import konra.hacksito.client.module.ModuleManager; 

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void onUpdatePose(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Verifica si el módulo está activo
        // Si tu ModuleManager usa otra forma de acceder, cámbialo aquí
        if (ModuleManager.getModule("Swimming").isEnabled()) {
            player.setPose(EntityPose.SWIMMING);
            ci.cancel(); 
        }
    }
}