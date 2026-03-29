package konra.hacksito.client.screen;

import konra.hacksito.client.module.AutoEatModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoEatConfigScreen extends Screen {
    
    public AutoEatConfigScreen() {
        super(Text.of("AutoEat Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("AutoEat");
        final AutoEatModule ae = (m instanceof AutoEatModule) ? (AutoEatModule)m : null;

        if (ae != null) {
            // 1. Slider de hambre (Hunger Threshold)
            double norm = ae.getEatThreshold() / 20.0;
            SliderWidget threshSlider = new SliderWidget(xMid - 100, yOffset, 200, 20, Text.empty(), norm) {
                @Override
                protected void updateMessage() {
                    int val = (int)(this.value * 20);
                    this.setMessage(Text.literal("Eat Below: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.valueOf(val)).formatted(Formatting.YELLOW)));
                }

                @Override
                protected void applyValue() {
                    ae.setEatThreshold((int)(this.value * 20));
                }
            };
            threshSlider.setMessage(Text.literal("Eat Below: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(ae.getEatThreshold())).formatted(Formatting.YELLOW)));
            this.addDrawableChild(threshSlider);
            
            yOffset += 26;

            // 2. Botón de Toggle (en lugar de Checkbox) para "Move while eating"
            ButtonWidget moveBtn = ButtonWidget.builder(getLabelText("Move While Eating", ae.isMoveWhileEating()), btn -> {
                ae.setMoveWhileEating(!ae.isMoveWhileEating());
                btn.setMessage(getLabelText("Move While Eating", ae.isMoveWhileEating()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build();
            
            this.addDrawableChild(moveBtn);
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
        // 1. Fondo elegante degradado
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // 2. Scissor para el área central
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER FIJO - QE CLIENT
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7AUTOEAT", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Etiqueta informativa
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Alimentación automática cuando baja el hambre", this.width / 2, 120, 0x888888);
    }
}