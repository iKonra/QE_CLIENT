package konra.hacksito.client.screen;

import konra.hacksito.client.module.ClickHelperModule;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ClickHelperConfigScreen extends Screen {
    private final Screen parent;

    public ClickHelperConfigScreen(Screen parent) {
        super(Text.of("ClickHelper Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int xMid = this.width / 2;
        ClickHelperModule module = (ClickHelperModule) ModuleManager.getModule("ClickHelper");

        this.addDrawableChild(new SliderWidget(xMid - 100, 100, 200, 20, Text.of("Extra Clicks"), (double) module.extraClicks / 20.0) {
    @Override
    protected void updateMessage() {
        int val = (int) (this.value * 20);
        String color = val > 10 ? "§c" : "§b";
        String warning = val > 10 ? " §4[DANGER]" : "";
        this.setMessage(Text.literal(color + "Boost: " + val + " CPS" + warning));
    }

    @Override
    protected void applyValue() {
        module.extraClicks = Math.max(1, (int) (this.value * 20));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.value > 0.5) { 
            context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0xFFFF0000);
        }
    }
});

        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER Y GUARDAR"), btn -> this.client.setScreen(parent))
                .dimensions(xMid - 60, this.height - 32, 120, 22).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);
        
        super.render(context, mouseX, mouseY, delta);

        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); 
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7CLICK HELPER SETTINGS", this.width / 2, 16, 0xFFFFFF);

        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);

        ClickHelperModule module = (ClickHelperModule) ModuleManager.getModule("ClickHelper");
        String desc = module.extraClicks > 10 ? "§4ADVERTENCIA: §cValores altos pueden llevar a una sancion." : "§7Usa este rango para pasar desapercibido.";
        context.drawCenteredTextWithShadow(this.textRenderer, desc, this.width / 2, 130, 0xFFFFFF);
    }
}