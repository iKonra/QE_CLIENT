package konra.hacksito.client.screen;

import konra.hacksito.client.module.XrayModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class XrayConfigScreen extends Screen {
    
    public XrayConfigScreen() {
        super(Text.of("Xray Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        ButtonWidget hintBtn = ButtonWidget.builder(Text.literal("").append(HackConfigScreen.getHintText()), btn -> {
            XrayModule.showEnableHint = !XrayModule.showEnableHint;
            btn.setMessage(HackConfigScreen.getHintText());
        }).dimensions(xMid - 100, yOffset, 200, 20).build();
        this.addDrawableChild(hintBtn);

        yOffset += 26;

        ButtonWidget itemsBtn = ButtonWidget.builder(Text.literal("").append(HackConfigScreen.getItemsText()), btn -> {
            XrayModule.showItems = !XrayModule.showItems;
            btn.setMessage(HackConfigScreen.getItemsText());
        }).dimensions(xMid - 100, yOffset, 200, 20).build();
        this.addDrawableChild(itemsBtn);

        yOffset += 26;

        ButtonWidget exposeBtn = ButtonWidget.builder(Text.literal("").append(HackConfigScreen.getExposedText()), btn -> {
            XrayModule.onlyExposed = !XrayModule.onlyExposed;
            btn.setMessage(HackConfigScreen.getExposedText());
        }).dimensions(xMid - 100, yOffset, 200, 20).build();
        this.addDrawableChild(exposeBtn);

        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); 
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lXRAY §fSETTINGS", this.width / 2, 16, 0xFFFFFF);

        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Configura el filtrado de minerales y bloques", this.width / 2, 145, 0xAAAAAA);
    }
}