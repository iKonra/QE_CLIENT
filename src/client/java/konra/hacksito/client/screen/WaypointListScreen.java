package konra.hacksito.client.screen;

import konra.hacksito.client.module.WaypointModule;
import konra.hacksito.client.utils.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class WaypointListScreen extends Screen {
    private final WaypointModule module;

    public WaypointListScreen(WaypointModule module) {
        super(Text.of("Gestión de Waypoints"));
        this.module = module;
    }

    @Override
    protected void init() {
        int centerX = width / 2;

        // --- BOTÓN PARA CREAR NUEVO ---
        this.addDrawableChild(ButtonWidget.builder(Text.of("§a+ Nuevo Waypoint"), button -> {
            client.setScreen(new WaypointScreen(module));
        }).dimensions(centerX - 50, 10, 100, 20).build());

        // --- BOTÓN CERRAR ---
        this.addDrawableChild(ButtonWidget.builder(Text.of("Cerrar"), button -> this.close())
                .dimensions(centerX - 50, height - 30, 100, 20).build());

        // --- LISTA DE WAYPOINTS CON BOTONES ---
        List<Waypoint> waypoints = module.getWaypoints();
        int yOffset = 45;

        for (int i = 0; i < waypoints.size(); i++) {
            final int index = i;
            final Waypoint wp = waypoints.get(i);

            // --- BOTÓN EDITAR ---
            this.addDrawableChild(ButtonWidget.builder(Text.of("⚙"), button -> {
                client.setScreen(new WaypointScreen(module, wp)); 
            }).dimensions(centerX + 35, yOffset, 20, 20).build());

            // --- BOTÓN ELIMINAR ---
            this.addDrawableChild(ButtonWidget.builder(Text.of("§cEliminar"), button -> {
                module.getWaypoints().remove(index);
                module.save(); 
                this.clearAndInit(); 
            }).dimensions(centerX + 60, yOffset, 60, 20).build());

            yOffset += 25;
            if (yOffset > height - 60) break;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Fondo con estética Hacksito
        context.fillGradient(0, 0, this.width, this.height, 0xF00A0A0A, 0xF0141414);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§l§bQE §fWAYPOINTS", width / 2, 35, 0xFFFFFF);

        int centerX = width / 2;
        int yOffset = 50;

        for (Waypoint wp : module.getWaypoints()) {
            // --- PROTECCIÓN CONTRA NULOS Y MEJORA DE TEXTO ---
            String dimStr = (wp.dimension != null) ? wp.dimension : "unknown";
            
            String dimName;
            if (dimStr.contains("overworld")) dimName = "§aOW";
            else if (dimStr.contains("nether")) dimName = "§cNE";
            else if (dimStr.contains("end")) dimName = "§dEN";
            else dimName = "§7??"; // Por si es null o dimensión de mod
            
            String info = dimName + " §b" + wp.name + " §7(" + (int)wp.pos.x + ", " + (int)wp.pos.y + ", " + (int)wp.pos.z + ")";
            context.drawTextWithShadow(this.textRenderer, info, centerX - 120, yOffset, 0xFFFFFF);
            
            // Cuadradito de color
            int color = (0xFF << 24) | ((int)(wp.r * 255) << 16) | ((int)(wp.g * 255) << 8) | (int)(wp.b * 255);
            context.fill(centerX - 135, yOffset, centerX - 125, yOffset + 10, color);

            yOffset += 25;
            if (yOffset > height - 60) break;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}