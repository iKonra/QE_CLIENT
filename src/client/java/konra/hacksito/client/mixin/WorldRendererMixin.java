package konra.hacksito.client.mixin;

import konra.hacksito.client.module.MobESPModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, 
                         Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, 
                         Matrix4f projectionMatrix, CallbackInfo ci) {
        
        // Obtenemos la posición de la cámara
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        // Llamamos al render del MobESP
        MobESPModule mobEsp = (MobESPModule) ModuleManager.getModule("MobESP");
        if (mobEsp != null && mobEsp.isEnabled()) {
            mobEsp.render(matrices, camX, camY, camZ);
        }
        
        // (Opcional) Aquí también deberías llamar al PlayerESP si lo tienes
    }
}