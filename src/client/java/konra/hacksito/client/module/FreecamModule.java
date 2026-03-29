package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

public class FreecamModule extends Module {
    public Vec3d camPos;
    public float camYaw;
    public float camPitch;
    private Vec3d playerPos;
    private float savedYaw;
    private float savedPitch;

    public FreecamModule() {
        super("Freecam");
    }

    @Override
    public void onEnable() {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        playerPos = mc.player.getPos();
        savedYaw = mc.player.getYaw();
        savedPitch = mc.player.getPitch();
        camPos = mc.player.getCameraPosVec(1.0f);
        camYaw = savedYaw;
        camPitch = savedPitch;
        mc.player.noClip = true;
        mc.player.sendMessage(Text.literal("§b[QE CLIENT] §fFreecam §aON"), true);
    }

    @Override
    public void onDisable() {
        var mc = MinecraftClient.getInstance();
        playerPos = null; 

        if (mc.player != null) {
            mc.player.noClip = false;
            mc.player.setVelocity(Vec3d.ZERO);
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
            
            mc.player.sendMessage(Text.literal("§b[QE CLIENT] §fFreecam §cOFF"), true);
        }
        if (mc.mouse != null) {
            mc.mouse.lockCursor();
        }
    }

    @Override
    public void onTick() {
        var mc = MinecraftClient.getInstance();
        if (!enabled || mc.player == null || playerPos == null) return;
        mc.player.setPos(playerPos.x, playerPos.y, playerPos.z);
        mc.player.setVelocity(Vec3d.ZERO);
    }
    public Vec3d getCamPos() { return camPos; }
    public float getCamYaw() { return camYaw; }
    public float getCamPitch() { return camPitch; }
}