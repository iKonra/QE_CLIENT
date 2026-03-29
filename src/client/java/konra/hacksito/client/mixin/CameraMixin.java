package konra.hacksito.client.mixin;

import konra.hacksito.client.module.FreecamModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        FreecamModule mod = (FreecamModule) ModuleManager.getModule("Freecam");
        
        // Seguridad: Solo actuar si el módulo está activo y tiene datos
        if (mod == null || !mod.isEnabled() || mod.camPos == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // 1. Sincronizar Rotación (Usa el movimiento del mouse del player)
        mod.camYaw = mc.player.getYaw();
        mod.camPitch = mc.player.getPitch();

        // 2. Procesar Movimiento por Frame (Fluidez total)
        // Ajustamos la velocidad base. 0.5 es un valor estándar equilibrado.
        double speed = 0.5; 
        double frameSpeed = speed * mc.getLastFrameDuration();

        double yawRad = Math.toRadians(mod.camYaw);
        double pitchRad = Math.toRadians(mod.camPitch);

        // Vector Forward (Hacia donde miras, incluyendo arriba/abajo)
        Vec3d forward = new Vec3d(
            -Math.sin(yawRad) * Math.cos(pitchRad),
            -Math.sin(pitchRad),
            Math.cos(yawRad) * Math.cos(pitchRad)
        ).multiply(frameSpeed);

        // Vector Strafe (Izquierda/Derecha - Corregido para no estar invertido)
        Vec3d strafe = new Vec3d(
            Math.cos(yawRad), 
            0, 
            Math.sin(yawRad)
        ).multiply(frameSpeed);

        // Aplicar inputs de teclado a la posición de la cámara libre
        if (mc.options.forwardKey.isPressed()) mod.camPos = mod.camPos.add(forward);
        if (mc.options.backKey.isPressed()) mod.camPos = mod.camPos.subtract(forward);
        
        // Fix de inversión: Left resta strafe, Right suma strafe
        if (mc.options.leftKey.isPressed()) mod.camPos = mod.camPos.add(strafe);
        if (mc.options.rightKey.isPressed()) mod.camPos = mod.camPos.subtract(strafe);
        
        // Movimiento vertical absoluto (Espacio / Shift)
        if (mc.options.jumpKey.isPressed()) mod.camPos = mod.camPos.add(0, frameSpeed, 0);
        if (mc.options.sneakKey.isPressed()) mod.camPos = mod.camPos.add(0, -frameSpeed, 0);

        // 3. Aplicar los valores finales a la cámara usando el Accessor
        CameraAccessor acc = (CameraAccessor) (Object) this;
        acc.invokeSetPos(mod.camPos);
        acc.invokeSetRotation(mod.camYaw, mod.camPitch);
    }
}