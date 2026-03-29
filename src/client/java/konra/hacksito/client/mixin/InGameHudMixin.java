package konra.hacksito.client.mixin;

import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        // No renderizar si el F1 está activado o si estamos en el menú de pausa
        if (mc.options.hudHidden || mc.currentScreen != null) return;

        int yOffset = 5; // Empezamos un poquito más arriba
        int color = 0x55FFFF; // Un color Cyan fachero (podes cambiarlo a tu gusto)

        List<Module> modules = ModuleManager.getModules();
        
        // Obtenemos el ancho de la ventana para calcular la derecha
        int screenWidth = mc.getWindow().getScaledWidth();

        for (Module mod : modules) {
            if (mod.isEnabled()) {
                String text = mod.getName();
                
                // Calculamos el ancho de cada palabra para que pegue al borde derecho
                int textWidth = mc.textRenderer.getWidth(text);
                
                // Dibujamos: pantalla total - ancho del texto - un pequeño margen (5)
                context.drawTextWithShadow(
                    mc.textRenderer, 
                    text, 
                    screenWidth - textWidth - 5, 
                    yOffset, 
                    color
                );
                
                yOffset += 10; // Espaciado entre hacks
            }
        }
    }
}