package konra.hacksito.client.screen;

import konra.hacksito.client.module.AimAssistModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AimAssistConfigScreen extends Screen {
    private final Screen parent;
    // Movido aquí para que render() lo vea
    private final int yStart = 70; 

    public AimAssistConfigScreen(Screen parent) {
        super(Text.of("AimAssist Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int xMid = this.width / 2;
        int buttonW = 200;

        AimAssistModule module = (AimAssistModule) ModuleManager.getModule("AimAssist");

        // Botón LEGIT
        this.addDrawableChild(ButtonWidget.builder(getModeText("LEGIT", module), btn -> {
            module.modeName = "LEGIT";
            btn.setMessage(getModeText("LEGIT", module));
            this.clearAndInit(); // Refresca los botones para actualizar los círculos ● / ○
        }).dimensions(xMid - (buttonW / 2), yStart, buttonW, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getModeText("MEDIUM", module), btn -> {
            module.modeName = "MEDIUM";
            this.clearAndInit();
        }).dimensions(xMid - (buttonW / 2), yStart + 25, buttonW, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getModeText("BLATANT", module), btn -> {
            module.modeName = "BLATANT";
            this.clearAndInit();
        }).dimensions(xMid - (buttonW / 2), yStart + 50, buttonW, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER Y GUARDAR"), btn -> this.client.setScreen(parent))
                .dimensions(xMid - 60, this.height - 32, 120, 22).build());
    }

    private Text getModeText(String mode, AimAssistModule module) {

        boolean active = module.modeName.equalsIgnoreCase(mode);
        String prefix = active ? "§b● " : "§8○ ";
        return Text.literal(prefix + mode).formatted(active ? Formatting.WHITE : Formatting.GRAY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);
        super.render(context, mouseX, mouseY, delta);

        // HEADER
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); 
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7AIMASSIST MODES", this.width / 2, 16, 0xFFFFFF);

        // FOOTER
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // El texto de abajo ahora reconoce yStart
        context.drawCenteredTextWithShadow(this.textRenderer, "§8Usa §aLEGIT §8para servidores con AntiCheat", this.width / 2, yStart + 85, 0xAAAAAA);
    }
}