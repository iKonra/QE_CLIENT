package konra.hacksito.client.module;


import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;

public class SpeedModule extends Module {

    public SpeedModule() {
        super("Speed");
    }

    @Override
    public void onEnable() {
        applySpeed();
    }

    @Override
    public void onDisable() {
        applyDefaultSpeed();
    }

    @Override
    public void onTick() {
        // forzar el valor base deseado cada tick; sprint u otros efectos
        // pueden cambiar el atributo temporalmente, asi que lo reiniciamos
        if (enabled) {
            applySpeed();
        }
    }

    private void applySpeed() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            var speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() != 0.15) {
                speed.setBaseValue(0.15);
            }
        }
    }

    private void applyDefaultSpeed() {
        var player = MinecraftClient.getInstance().player;
        if (player != null) {
            var speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() != 0.1) {
                speed.setBaseValue(0.1);
            }
        }
    }
}