package konra.hacksito.client.screen;

import konra.hacksito.client.module.AutoToolModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoToolConfigScreen extends Screen {
    public AutoToolConfigScreen() {
        super(Text.of("AutoTool Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("AutoTool");
        final AutoToolModule at = (m instanceof AutoToolModule) ? (AutoToolModule)m : null;

        if (at != null) {
            // 1. Toggle Swords
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Use Swords", at.isUseSwords()), btn -> {
                at.setUseSwords(!at.isUseSwords());
                btn.setMessage(getLabelText("Use Swords", at.isUseSwords()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 24;

            // 2. Toggle Hands
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Use Hands", at.isUseHands()), btn -> {
                at.setUseHands(!at.isUseHands());
                btn.setMessage(getLabelText("Use Hands", at.isUseHands()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 24;

            // 3. Toggle Switch Back
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Switch Back", at.isSwitchBack()), btn -> {
                at.setSwitchBack(!at.isSwitchBack());
                btn.setMessage(getLabelText("Switch Back", at.isSwitchBack()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            yOffset += 28;

            // 4. Slider de Reparación
            double initialVal = at.getRepairMode() / 100.0;
            SliderWidget repairSlider = new SliderWidget(xMid - 100, yOffset, 200, 20, Text.empty(), initialVal) {
                @Override
                protected void updateMessage() {
                    int val = (int)(this.value * 100);
                    this.setMessage(Text.literal("Repair Threshold: ").formatted(Formatting.GRAY)
                        .append(Text.literal(val + "%").formatted(Formatting.YELLOW)));
                }

                @Override
                protected void applyValue() {
                    at.setRepairMode((int)(this.value * 100));
                }
            };
            repairSlider.setMessage(Text.literal("Repair Threshold: ").formatted(Formatting.GRAY)
                .append(Text.literal(at.getRepairMode() + "%").formatted(Formatting.YELLOW)));
            this.addDrawableChild(repairSlider);
        }

        // Botón VOLVER (Estética QE Client)
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
        // 1. Fondo elegante
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // 2. Scissor para el área central
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER FIJO - QE CLIENT
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); 
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7AUTOTOOL", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Info extra
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Cambia automáticamente a la mejor herramienta", this.width / 2, 165, 0x888888);
    }
}