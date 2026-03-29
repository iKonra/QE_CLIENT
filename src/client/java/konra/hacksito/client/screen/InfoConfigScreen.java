package konra.hacksito.client.screen;

import konra.hacksito.client.module.InfoModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class InfoConfigScreen extends Screen {
    
    public InfoConfigScreen() {
        super(Text.of("Info Settings"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("Info");
        final InfoModule im = (m instanceof InfoModule) ? (InfoModule)m : null;

        if (im != null) {
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Show CPS", im.isShowCPS()), btn -> {
                im.setShowCPS(!im.isShowCPS());
                btn.setMessage(getLabelText("Show CPS", im.isShowCPS()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 24;

            this.addDrawableChild(ButtonWidget.builder(getLabelText("Show Armor", im.isShowArmor()), btn -> {
                im.setShowArmor(!im.isShowArmor());
                btn.setMessage(getLabelText("Show Armor", im.isShowArmor()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 24;

            this.addDrawableChild(ButtonWidget.builder(getLabelText("Show Potions", im.isShowPots()), btn -> {
                im.setShowPots(!im.isShowPots());
                btn.setMessage(getLabelText("Show Potions", im.isShowPots()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 24;

            this.addDrawableChild(ButtonWidget.builder(getLabelText("Show Distance", im.isShowDistance()), btn -> {
                im.setShowDistance(!im.isShowDistance());
                btn.setMessage(getLabelText("Show Distance", im.isShowDistance()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    private Text getLabelText(String label, boolean active) {
        return Text.literal(label + ": ").formatted(Formatting.GRAY)
            .append(Text.literal(active ? "ON" : "OFF").formatted(active ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); 
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7INFO SETTINGS", this.width / 2, 16, 0xFFFFFF);

        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        Module m = ModuleManager.getModule("Info");
        if (m != null && !m.isEnabled()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§c¡AVISO: El módulo Info está desactivado!", this.width / 2, 170, 0xFF5555);
        }
    }
}