package konra.hacksito.client.screen;

import konra.hacksito.client.module.MobESPModule;
import konra.hacksito.client.module.Module;
import konra.hacksito.client.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MobESPConfigScreen extends Screen {

    public MobESPConfigScreen() {
        super(Text.of("MobESP Configuration"));
    }

    @Override
    protected void init() {
        super.init();
        int xMid = this.width / 2;
        int yOffset = 65; 

        // Obtenemos el módulo para vincular los botones a sus funciones
        Module m = ModuleManager.getModule("MobESP");
        final MobESPModule mem = (m instanceof MobESPModule) ? (MobESPModule)m : null;

        if (mem != null) {
            // 1. Botón para Filtrar Invisibles
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Filter Invisible", mem.isFilterInvisible()), btn -> {
                mem.setFilterInvisible(!mem.isFilterInvisible());
                btn.setMessage(getLabelText("Filter Invisible", mem.isFilterInvisible()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            
            yOffset += 26;

            // 2. Botón para Ocultar Pasivos (Vacas, Cerdos, etc.)
            this.addDrawableChild(ButtonWidget.builder(getLabelText("Hide Passives", mem.isFilterPassive()), btn -> {
                mem.setFilterPassive(!mem.isFilterPassive());
                btn.setMessage(getLabelText("Hide Passives", mem.isFilterPassive()));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
            
            yOffset += 26;
            
            // 3. Botón para Alternar Estilo (Cajas / Líneas)
            // Nota: Aquí reutilizamos el enum de PlayerESP para mantener consistencia
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Style: ").formatted(Formatting.GRAY).append(Text.literal(mem.getStyle().name()).formatted(Formatting.AQUA)), btn -> {
                // Lógica simple para rotar entre los estilos del Enum
                int nextOrd = (mem.getStyle().ordinal() + 1) % konra.hacksito.client.module.PlayerESPModule.EspStyle.values().length;
                mem.setStyle(konra.hacksito.client.module.PlayerESPModule.EspStyle.values()[nextOrd]);
                btn.setMessage(Text.literal("Style: ").formatted(Formatting.GRAY).append(Text.literal(mem.getStyle().name()).formatted(Formatting.AQUA)));
            }).dimensions(xMid - 100, yOffset, 200, 20).build());
        }

        // Botón VOLVER (Estética QE Client)
        this.addDrawableChild(ButtonWidget.builder(Text.of("§cVOLVER"), btn -> {
            if (this.client != null) this.client.setScreen(new HackConfigScreen());
        }).dimensions(xMid - 60, this.height - 35, 120, 22).build());
    }

    // Método auxiliar para generar los textos ON/OFF con colores
    private Text getLabelText(String label, boolean active) {
        return Text.literal(label + ": ").formatted(Formatting.GRAY)
            .append(Text.literal(active ? "ON" : "OFF").formatted(active ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fondo oscuro degradado
        context.fillGradient(0, 0, this.width, this.height, 0xFA0A0A0A, 0xFA141414);

        // Renderizado de botones y widgets
        super.render(context, mouseX, mouseY, delta);

        // HEADER FIJO - QE CLIENT
        context.fill(0, 0, this.width, 42, 0xFF000000); 
        context.fill(0, 40, this.width, 41, 0xFF55FFFF); // Línea cian de marca
        context.drawCenteredTextWithShadow(this.textRenderer, "§b§lQE §fCLIENT §8| §7MOB ESP SETTINGS", this.width / 2, 16, 0xFFFFFF);

        // FOOTER FIJO
        context.fill(0, this.height - 42, this.width, this.height, 0xFF000000);
        context.fill(0, this.height - 42, this.width, this.height - 41, 0xFF55FFFF);
        
        // Texto descriptivo abajo
        context.drawCenteredTextWithShadow(this.textRenderer, "§8Rojo: Hostiles | Verde: Pasivos", this.width / 2, 150, 0x888888);
    }
    
}