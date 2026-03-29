package konra.hacksito.client.mixin;

import konra.hacksito.client.module.XrayModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
   @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
private static void onShouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos neighborPos, CallbackInfoReturnable<Boolean> info) {
    if (XrayModule.isXrayEnabled()) {
        // Obtenemos el bloque
        net.minecraft.block.Block block = state.getBlock();
        
        // CONDICIÓN ESTRICTA:
        // Si el bloque está en la lista blanca, lo dibujamos (true).
        // Si NO está en la lista, lo ocultamos (false).
        if (XrayModule.XRAY_BLOCKS.contains(block)) {
            info.setReturnValue(true);
        } else {
            info.setReturnValue(false); 
        }
        // Al usar setReturnValue, cancelamos la lógica normal de Minecraft.
    }
}
}