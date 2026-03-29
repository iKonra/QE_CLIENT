package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

public class AimAssistModule extends Module {
    
    public String modeName = "MEDIUM"; 

    public enum Mode {
        // Rango corto, FOV cerrado y velocidad muy baja para bypass
        LEGIT(3.0f, 20f, 0.15f, "§aLEGIT"),      
        // Rango normal y velocidad estándar
        MEDIUM(4.5f, 60f, 0.35f, "§eMEDIUM"),    
        // Rango largo y velocidad casi instantánea
        BLATANT(6.0f, 360f, 0.85f, "§cBLATANT"); 

        final float range, fov, speed;
        final String displayName;

        Mode(float range, float fov, float speed, String displayName) {
            this.range = range;
            this.fov = fov;
            this.speed = speed;
            this.displayName = displayName;
        }
    }

    public AimAssistModule() {
        super("AimAssist");
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}
    @Override public void onTick() {}

    public Mode getCurrentMode() {
        try { return Mode.valueOf(modeName); } catch (Exception e) { return Mode.MEDIUM; }
    }

    public void onRender(float tickDelta) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || !isEnabled() || mc.currentScreen != null) return;

        Mode mode = getCurrentMode();
        if (!isHoldingWeapon(mc)) return;

        LivingEntity target = getClosestTarget(mc, mode);

        if (target != null) {
            float[] targetRots = getRotationsTo(target, tickDelta);
            
            // Lógica de suavizado mejorada:
            // Cuanto más cerca esté la mira del objetivo, más lento se mueve (Logarithmic smoothing)
            float yawDiff = Math.abs(getAngleDifference(mc.player.getYaw(), targetRots[0]));
            float finalSpeed = mode.speed;
            
            if (mode == Mode.LEGIT) {
                // Si la diferencia es poca, bajamos la velocidad drásticamente para no "clavarnos"
                if (yawDiff < 5f) finalSpeed *= 0.25f;
                else finalSpeed *= 0.45f;
            }

            mc.player.setYaw(updateRotation(mc.player.getYaw(), targetRots[0], finalSpeed));
            mc.player.setPitch(updateRotation(mc.player.getPitch(), targetRots[1], finalSpeed));
        }
    }

    private LivingEntity getClosestTarget(MinecraftClient mc, Mode mode) {
        LivingEntity closest = null;
        double closestDist = mode.range;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living) || entity == mc.player || !entity.isAlive() || entity.isInvisible()) continue;

            // --- WALL CHECK (RAYCAST) ---
            // Si el jugador no puede ver al objetivo, lo ignora
            if (!mc.player.canSee(entity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= closestDist && isInFov(living, mode)) {
                closest = living;
                closestDist = dist;
            }
        }
        return closest;
    }

    private boolean isInFov(LivingEntity target, Mode mode) {
        var mc = MinecraftClient.getInstance();
        float[] rotations = getRotationsTo(target, 0);
        float yawDiff = Math.abs(getAngleDifference(mc.player.getYaw(), rotations[0]));
        return yawDiff <= mode.fov;
    }

    private boolean isHoldingWeapon(MinecraftClient mc) {
        ItemStack stack = mc.player.getMainHandStack();
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem;
    }

    private float updateRotation(float current, float target, float step) {
        float diff = getAngleDifference(current, target);
        return current + (diff * step);
    }

    private float getAngleDifference(float a, float b) {
        return MathHelper.wrapDegrees(b - a);
    }

    private float[] getRotationsTo(LivingEntity entity, float tickDelta) {
        var mc = MinecraftClient.getInstance();
        double x = MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
        
        // Apuntamos al pecho (EyeHeight - 0.4) para ser más discretos
        Vec3d targetPos = new Vec3d(x, y + entity.getEyeHeight(entity.getPose()) - 0.4, z);
        Vec3d diff = targetPos.subtract(mc.player.getEyePos());
        
        double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
        float pitch = (float) MathHelper.wrapDegrees(-Math.toDegrees(Math.atan2(diff.y, dist)));
        
        return new float[]{yaw, pitch};
    }
}