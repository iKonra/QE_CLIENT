package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoEatModule extends Module {
    // umbral de hambre por debajo del cual empezamos a comer (0-20)
    private int eatThreshold = 18;
    // si dejamos mover mientras comemos (más lento, sprint sigue)
    private boolean moveWhileEating = true;

    public AutoEatModule() {
        super("AutoEat");
    }

    public int getEatThreshold() {
        return eatThreshold;
    }

    public void setEatThreshold(int eatThreshold) {
        this.eatThreshold = eatThreshold;
    }

    public boolean isMoveWhileEating() {
        return moveWhileEating;
    }
    public void setMoveWhileEating(boolean moveWhileEating) {
        this.moveWhileEating = moveWhileEating;
    }

    @Override
    public void onEnable() {
        // nada pa init
    }

    @Override
    public void onDisable() {
        // nada pa limpiar
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        var client = MinecraftClient.getInstance();
        var player = client.player;
        if (player == null) return;

        if (player.getHungerManager().getFoodLevel() >= eatThreshold) {
            return; // todavia hay hambre
        }
        // si justo empezamos a comer o estamos comiendo, opcionalmente mantener mov
        if (player.isUsingItem()) {
            if (moveWhileEating) {
                player.setSprinting(true);
            }
            return;
        }

        // buscar la comida con mayor suma hambre+saturacion
        int bestSlot = -1;
        float bestScore = 0f;
        for (int i = 0; i < player.getInventory().main.size(); i++) {
            ItemStack stack = player.getInventory().main.get(i);
            if (stack.isFood()) {
                var comp = stack.getItem().getFoodComponent();
                if (comp != null) {
                    float score = comp.getHunger() + comp.getSaturationModifier();
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = i;
                    }
                }
            }
        }
        if (bestSlot != -1) {
            player.getInventory().selectedSlot = bestSlot;
            // empezar a comer explícito
            player.setCurrentHand(Hand.MAIN_HAND);
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
            if (moveWhileEating) {
                client.options.useKey.setPressed(true);
            }
        }
    }
}