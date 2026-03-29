package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.text.Text;
import java.util.Random;

public class ClickHelperModule extends Module {

    public int extraClicks = 4; // Clics TOTALES a sumar por segundo
    private int userClickCount = 0;
    private int timerTicks = 0;
    private final Random random = new Random();
    private boolean isDangerousZone = false;

    public ClickHelperModule() {
        super("ClickHelper");
    }

    @Override public void onEnable() {
        userClickCount = 0;
        isDangerousZone = false;
    }
    @Override
    public void onDisable() {
        isDangerousZone = false;
        userClickCount = 0;
    }

    @Override
    public void onTick() {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        
        if (timerTicks > 0) {
            timerTicks--;
        } else {
            if (userClickCount > 0) userClickCount--;
            timerTicks = 5; 
        }

        updateZone(mc);
    
        if (mc.options.attackKey.isPressed() && mc.player.age % 2 == 0) {
            userClickCount = Math.min(userClickCount + 1, 15);
            timerTicks = 20; 
        }

        if (isDangerousZone && mc.options.attackKey.isPressed()) {
            float probability = (float) extraClicks / 20.0f;
            if (random.nextFloat() < probability) {
                doSingleExtraClick(mc);
            }
        }
    }

    private void updateZone(MinecraftClient mc) {
        if (userClickCount >= 5 && !isDangerousZone) {
            isDangerousZone = true;
            mc.player.sendMessage(Text.literal("§6[QE] §cZONA PELIGROSA: §eON"), true);
        } else if (userClickCount < 2 && isDangerousZone) {
            isDangerousZone = false;
            mc.player.sendMessage(Text.literal("§6[QE] §aZONA SEGURA"), true);
        }
    }

    private void doSingleExtraClick(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            mc.interactionManager.attackEntity(mc.player, ((EntityHitResult) hit).getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
            
            Module info = ModuleManager.getModule("Info");
            if (info instanceof InfoModule) {
                ((InfoModule) info).registerClick();
            }
        }
    }
}