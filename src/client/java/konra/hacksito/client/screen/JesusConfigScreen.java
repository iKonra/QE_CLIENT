package konra.hacksito.client.screen;

import konra.hacksito.client.module.JesusModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class JesusConfigScreen extends Screen {
    
    public JesusConfigScreen() {
        super(Text.of("Jesus Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("Jesus");
        final JesusModule jm = (m instanceof JesusModule) ? (JesusModule)m : null;

        if (jm != null) {
            // Botón de Toggle para el Bypass
            ButtonWidget bypassBtn = ButtonWidget.builder(getBypassText(jm), btn -> {
                jm.setBypass(!jm.isBypass());
                btn.setMessage(getBypassText(jm));
            }).dimensions(xMid - 100, yOffset, 200, 20).build();
            
            this.addDrawableChild(bypassBtn);
        }

        // Botón VOLVER (Estética idéntica a los demás)
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    private static Text getBypassText(JesusModule jm) {
        boolean on = jm != null && jm.isBypass();
        return Text.literal("NCP Bypass: ").formatted(Formatting.GRAY)
            .append(Text.literal(on ? "ON" : "OFF").formatted(on ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Fondo degradado profesional
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // 2. Scissor para mantener los widgets en su lugar
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian neón
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lJESUS §fSETTINGS", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Texto de descripción
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Camina sobre el agua (Bypass para servidores con NCP)", this.width / 2, 95, 0xAAAAAA);
    }
}