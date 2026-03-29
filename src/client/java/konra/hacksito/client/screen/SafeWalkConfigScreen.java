package konra.hacksito.client.screen;

import konra.hacksito.client.module.SafeWalkModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SafeWalkConfigScreen extends Screen {
    
    public SafeWalkConfigScreen() {
        super(Text.of("SafeWalk Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("SafeWalk");
        final SafeWalkModule sw = (m instanceof SafeWalkModule) ? (SafeWalkModule)m : null;

        if (sw != null) {
            // Reemplazamos el Checkbox por un botón de Toggle con nuestra estética
            ButtonWidget modeBtn = ButtonWidget.builder(getModeText(sw.isSlowMode()), btn -> {
                sw.setSlowMode(!sw.isSlowMode());
                btn.setMessage(getModeText(sw.isSlowMode()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build();
            
            this.addDrawableChild(modeBtn);
        }

        // Botón VOLVER (Estética idéntica al principal)
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    private Text getModeText(boolean active) {
        return Text.literal("Slow Mode: ").formatted(Formatting.GRAY)
            .append(Text.literal(active ? "§aON" : "§cOFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Fondo degradado oscuro
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // 2. Área central con Scissor
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER FIJO
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian neón
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lSAFEWALK §fSETTINGS", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Texto de ayuda
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Evita caerte de los bordes mientras caminas", this.width / 2, 95, 0xAAAAAA);
    }
}