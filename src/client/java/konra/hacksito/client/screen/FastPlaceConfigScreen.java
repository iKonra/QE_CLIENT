package konra.hacksito.client.screen;

import konra.hacksito.client.module.FastPlaceModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FastPlaceConfigScreen extends Screen {
    
    public FastPlaceConfigScreen() {
        super(Text.of("FastPlace Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        Module m = ModuleManager.getModule("FastPlace");
        final FastPlaceModule fp = (m instanceof FastPlaceModule) ? (FastPlaceModule)m : null;

        // Valor normalizado (0.0 a 1.0) para el Slider
        double initialValue = fp != null ? (fp.getSpeed() - 1.0) / 4.0 : 0.0;

        // Slider con la misma estética de colores
        SliderWidget speedSlider = new SliderWidget(xMid - 100, yOffset, 200, 20, Text.empty(), initialValue) {
            @Override
            protected void updateMessage() {
                float val = 1f + (float)(this.value * 4);
                this.setMessage(Text.literal("Speed: ").formatted(Formatting.GRAY)
                    .append(Text.literal(String.format("%.2fx", val)).formatted(Formatting.AQUA)));
            }

            @Override
            protected void applyValue() {
                if (fp != null) {
                    fp.setSpeed(1f + (float)(this.value * 4));
                }
            }
        };

        // Forzar mensaje inicial
        float currentSpeed = fp != null ? fp.getSpeed() : 1f;
        speedSlider.setMessage(Text.literal("Speed: ").formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.2fx", currentSpeed)).formatted(Formatting.AQUA)));
        
        this.addDrawableChild(speedSlider);

        // Botón Volver con la estética del botón Cerrar
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Fondo elegante degradado
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // 2. Área central (Scissor)
        context.enableScissor(0, 42, this.width, this.height - 42);
        super.render(context, mouseX, mouseY, delta);
        context.disableScissor();

        // 3. HEADER FIJO (Igual al principal)
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lFASTPLACE §fSETTINGS", this.width / 2, 16, 0xFFFFFF);

        // 4. FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // 5. Etiqueta informativa
        context.drawCenteredTextWithShadow(this.textRenderer, "§7Ajusta la velocidad de colocación de bloques", this.width / 2, 95, 0xAAAAAA);
    }
}