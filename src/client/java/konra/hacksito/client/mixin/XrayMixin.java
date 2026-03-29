package konra.hacksito.client.mixin;

import konra.hacksito.client.module.XrayModule;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class XrayMixin {

   @Inject(method = "isSideInvisible", at = @At("HEAD"), cancellable = true)
private void onIsSideInvisible(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> info) {
    if (XrayModule.isXrayEnabled()) {
        try {
            BlockState self = (BlockState) (Object) this;
            if (self == null || self.isAir()) return; // Evita procesar bloques que ya no existen

            if (XrayModule.XRAY_BLOCKS.contains(self.getBlock())) {
                info.setReturnValue(false);
            } else {
                info.setReturnValue(true);
            }
        } catch (Exception e) {
            // Si algo falla, dejamos que el juego decida normalmente para no crashear
        }
    }
}

    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
    private void onGetAmbientOcclusionLightLevel(CallbackInfoReturnable<Float> info) {
        if (XrayModule.isXrayEnabled()) {
            // Elimina sombras negras entre bloques para ver claro en túneles
            info.setReturnValue(1.0f);
        }
    }
}