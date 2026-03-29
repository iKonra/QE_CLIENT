package konra.hacksito.client.screen;

import konra.hacksito.client.module.HighJumpModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HighJumpConfigScreen extends Screen {
    
    public HighJumpConfigScreen() {
        super(Text.of("HighJump Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("HighJump");
        final HighJumpModule hj = (m instanceof HighJumpModule) ? (HighJumpModule)m : null;

        if (hj != null) {
            double normalized = hj.getHeight() / 100.0;

            // Slider de Altura Estilizado
            SliderWidget heightSlider = new SliderWidget(xMid - 100, yOffset, 200, 20, Text.empty(), normalized) {
                @Override
                protected void updateMessage() {
                    float val = (float)(this.value * 100);
                    this.setMessage(Text.literal("Jump Height: ").formatted(Formatting.GRAY)
                        .append(Text.literal(String.format("%.1f", val)).formatted(Formatting.YELLOW)));
                }

                @Override
                protected void applyValue() {
                    hj.setHeight((float)(this.value * 100));
                }
            };

            // Mensaje inicial del slider
            heightSlider.setMessage(Text.literal("Jump Height: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.format("%.1f", hj.getHeight())).formatted(Formatting.YELLOW)));
            
            this.addDrawableChild(heightSlider);
        }

        // Botón VOLVER (Estética QE Client)
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
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
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian neón
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7HIGHJUMP", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Instrucciones de uso (Sustituye al botón inactivo anterior)
        context.drawCenteredTextWithShadow(this.textRenderer, "§eTip: §7Presiona espacio dos veces rápido para el doble salto", this.width / 2, 95, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§8Ajusta la potencia del impulso vertical", this.width / 2, 110, 0x888888);
    }
}