package konra.hacksito.client.module;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Formatting;

public class InfoModule extends Module {
    private final Deque<Long> clicks = new ArrayDeque<>();
    private float lastDistance = 0f;

    // Configuración
    private boolean showCPS = true;
    private boolean showArmor = true;
    private boolean showPots = true;
    private boolean showDistance = true;

    public InfoModule() {
        super("Info");
    }

    // --- GETTERS Y SETTERS ---
    public boolean isShowCPS() { return showCPS; }
    public void setShowCPS(boolean v) { showCPS = v; }
    public boolean isShowArmor() { return showArmor; }
    public void setShowArmor(boolean v) { showArmor = v; }
    public boolean isShowPots() { return showPots; }
    public void setShowPots(boolean v) { showPots = v; }
    public boolean isShowDistance() { return showDistance; }
    public void setShowDistance(boolean v) { showDistance = v; }

    @Override
    public void onEnable() { clicks.clear(); }

    @Override
    public void onDisable() { clicks.clear(); }

    public void registerClick() {
        clicks.addLast(System.currentTimeMillis());
    }

    @Override
public void onTick() {
    // 1. Limpieza de clics antiguos (Mantener solo los del último segundo)
    long now = System.currentTimeMillis();
    while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
        clicks.removeFirst();
    }

    MinecraftClient client = MinecraftClient.getInstance();
    if (client.player == null) return;

    // 2. REGISTRO DE CLICS MANUALES
    // Detecta cada vez que presionas el botón de ataque (clic izquierdo)
    while (client.options.attackKey.wasPressed()) {
        registerClick();
    }

    // 3. LÓGICA DE DISTANCIA AL OBJETIVO (Raycast)
    var hit = client.crosshairTarget;
    if (hit != null && hit.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
        var e = ((net.minecraft.util.hit.EntityHitResult) hit).getEntity();
        // Calculamos la distancia entre tus ojos y la entidad
        lastDistance = (float) client.player.getEyePos().distanceTo(e.getPos().add(0, e.getEyeHeight(e.getPose()), 0));
        
        // Si prefieres distancia simple de pies a pies (menos precisa para PvP):
        // lastDistance = (float) client.player.getPos().distanceTo(e.getPos());
    } else {
        lastDistance = 0f;
    }
}

    public static void renderOverlay(DrawContext context) {
        InfoModule m = (InfoModule) ModuleManager.getModule("Info");
        if (m == null || !m.isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int x = 10;
        int y = 10;
        int white = 0xFFFFFF;

        // 1. CPS
        if (m.showCPS) {
            context.drawText(client.textRenderer, "CPS: " + Formatting.GREEN + m.clicks.size(), x, y, white, true);
            y += 12;
        }

        // 2. DISTANCIA
        if (m.showDistance && m.lastDistance > 0) {
            Formatting distColor = m.lastDistance <= 3.0f ? Formatting.RED : 
                                  (m.lastDistance <= 6.0f ? Formatting.YELLOW : Formatting.GREEN);
            
            context.drawText(client.textRenderer, "Dist: " + distColor + String.format("%.1f", m.lastDistance), x, y, white, true);
            y += 12;
        }

        // 3. POCIONES
        if (m.showPots) {
            int healPots = 0;
            for (ItemStack stack : client.player.getInventory().main) {
                if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                    if (PotionUtil.getPotion(stack) == Potions.HEALING || PotionUtil.getPotion(stack) == Potions.STRONG_HEALING) {
                        healPots += stack.getCount();
                    }
                }
            }
            
            ItemStack potIcon = new ItemStack(Items.SPLASH_POTION);
            PotionUtil.setPotion(potIcon, Potions.HEALING);
            
            context.drawItem(potIcon, x, y);
            context.drawText(client.textRenderer, " x " + healPots, x + 18, y + 4, white, true);
            y += 22; 
        }

        // 4. ARMADURA VERTICAL
        if (m.showArmor) {
            List<ItemStack> armor = new ArrayList<>();
            client.player.getArmorItems().forEach(armor::add);
            Collections.reverse(armor); 

            for (ItemStack stack : armor) {
                if (!stack.isEmpty()) {
                    // El método drawItem ya dibuja el ítem CON el efecto de brillo (glint)
                    context.drawItem(stack, x, y);
                    
                    if (stack.isDamageable()) {
                        int max = stack.getMaxDamage();
                        int current = max - stack.getDamage();
                        int percent = (current * 100) / max;
                        
                        Formatting color = percent > 70 ? Formatting.GREEN : (percent > 30 ? Formatting.YELLOW : Formatting.RED);
                        context.drawText(client.textRenderer, color + "" + percent + "%", x + 20, y + 4, 0xFFFFFF, true);
                    } else {
                        context.drawText(client.textRenderer, Formatting.GRAY + "100%", x + 20, y + 4, 0xFFFFFF, true);
                    }
                    y += 18; 
                }
            }
        }
    }
}