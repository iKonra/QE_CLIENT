package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.item.SwordItem;
import net.minecraft.item.Item;

public class AutoToolModule extends Module {
    private boolean useSwords = false;
    private boolean useHands = true;
    private int repairMode = 0;
    private boolean switchBack = false;

    private int prevSelectedSlot = -1;
    private boolean prevAttack = false;
    private BlockPos lastPos = null; // recordar el último bloque minado

    public AutoToolModule() {
        super("AutoTool");
    }

    // getters y setters de config ------------------------------------------------
    public boolean isUseSwords() {
        return useSwords;
    }
    public void setUseSwords(boolean value) {
        useSwords = value;
    }

    public boolean isUseHands() {
        return useHands;
    }
    public void setUseHands(boolean value) {
        useHands = value;
    }

    public int getRepairMode() {
        return repairMode;
    }
    public void setRepairMode(int value) {
        repairMode = Math.max(0, value);
    }

    public boolean isSwitchBack() {
        return switchBack;
    }
    public void setSwitchBack(boolean value) {
        switchBack = value;
    }

    // -------------------------------------------------------------------------------
    @Override
    public void onEnable() {
        prevSelectedSlot = -1;
        prevAttack = false;
    }

    @Override
    public void onDisable() {
        prevSelectedSlot = -1;
        prevAttack = false;
    }

    @Override
    public void onTick() {
        if (!enabled) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        boolean nowAttack = client.options.attackKey.isPressed();
        // crosshairTarget remplazo a hitResult en los mappings nuevos
        HitResult hit = client.crosshairTarget;

        // cuando el player le da click a un bloque, elegimos herramienta
        if (nowAttack && hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            if (pos != null) {
                // guardar slot una vez al empezar a picar
                if (!prevAttack && prevSelectedSlot == -1) {
                    prevSelectedSlot = client.player.getInventory().selectedSlot;
                }
                // equipar al iniciar o al pasar a otro bloque
                if (!pos.equals(lastPos) || !prevAttack) {
                    equipBestTool(pos);
                }
                lastPos = pos;
            }
        }

        // cuando el player deja de picar, opcionalmente cambiar de nuevo
        if (!nowAttack && prevAttack) {
            if (switchBack && prevSelectedSlot != -1) {
                client.player.getInventory().selectedSlot = prevSelectedSlot;
            }
            prevSelectedSlot = -1;
            lastPos = null;
        }

        prevAttack = nowAttack;
    }

    private void equipBestTool(BlockPos pos) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        // evitar cambiar herramientas en creativo (instabuild quitado)
        if (player.isCreative()) return;
        BlockState state = client.world.getBlockState(pos);
        // PlayerInventory expone el campo selectedSlot
        PlayerInventory inv = player.getInventory();
        ItemStack held = player.getMainHandStack();

        int currentSlot = inv.selectedSlot;
        float bestSpeed = getMiningSpeed(held, state);
        int bestSlot = currentSlot;

        if (isTooDamaged(held, repairMode)) {
            bestSpeed = 1f; // tratar herramienta dañada como débil
        }

        for (int slot = 0; slot < 9; slot++) {
            if (slot == currentSlot) continue;
            ItemStack stack = inv.getStack(slot);
            if (stack.isEmpty()) continue;

            float speed = getMiningSpeed(stack, state);
            if (isTooDamaged(stack, repairMode)) {
                speed = 1f;
            }
            if (speed <= bestSpeed) continue;
            if (!useSwords && stack.getItem() instanceof SwordItem) continue;

            bestSpeed = speed;
            bestSlot = slot;
        }

        if (bestSpeed <= 1f && useHands) {
            // ninguna herramienta es claramente mejor; capaz convenga mano vacia
            if (held != null && isWrongTool(held, state)) {
                int empty = findEmptySlot();
                if (empty != -1) bestSlot = empty;
            }
        }

        if (bestSlot != currentSlot) {
            inv.selectedSlot = bestSlot;
        }
    }

    private float getMiningSpeed(ItemStack stack, BlockState state) {
        if (stack == null || stack.isEmpty()) return 1f;
        // cambio de mapeo: la velocidad de minado es método de instancia de Item
        float speed = stack.getItem().getMiningSpeedMultiplier(stack, state);

        if (speed > 1) {
            // sumar enchant efficiency
            int lvl = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (lvl > 0) speed += lvl * lvl + 1;
        }
        return speed;
    }

    private boolean isTooDamaged(ItemStack stack, int repairThreshold) {
        return stack != null &&
               !stack.isEmpty() &&
               stack.isDamageable() &&
               stack.getMaxDamage() - stack.getDamage() <= repairThreshold;
    }

    private boolean isWrongTool(ItemStack stack, BlockState state) {
        return getMiningSpeed(stack, state) <= 1f;
    }

    private int findEmptySlot() {
        PlayerInventory inv = MinecraftClient.getInstance().player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isEmpty()) return i;
        }
        return -1;
    }
}