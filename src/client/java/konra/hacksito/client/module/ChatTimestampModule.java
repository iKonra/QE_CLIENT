package konra.hacksito.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChatTimestampModule extends Module {
    public ChatTimestampModule() {
        super("ChatTimestamp");
    }

    @Override
    public void onEnable() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("Chat timestamp ").append(Text.literal("activado").formatted(Formatting.GREEN)), false);
        }
    }

    @Override
    public void onDisable() {
        var mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("Chat timestamp ").append(Text.literal("desactivado").formatted(Formatting.RED)), false);
        }
    }
}