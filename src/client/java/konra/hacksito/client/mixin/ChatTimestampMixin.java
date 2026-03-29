package konra.hacksito.client.mixin;

import konra.hacksito.client.module.ChatTimestampModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Mixin(ChatHud.class)
public class ChatTimestampMixin {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", 
                    at = @At("HEAD"), 
                    argsOnly = true)
    private Text injectTimestamp(Text text) {
        var module = ModuleManager.getModule("ChatTimestamp");
        
        if (module != null && module.isEnabled()) {
            String time = LocalTime.now().format(TIME_FMT);
            // Creamos "[HH:mm] " en gris y le pegamos el mensaje original
            return Text.literal("[" + time + "] ")
                    .formatted(Formatting.GRAY)
                    .append(text);
        }
        
        return text; // Si el módulo está apagado, devuelve el mensaje normal
    }
}