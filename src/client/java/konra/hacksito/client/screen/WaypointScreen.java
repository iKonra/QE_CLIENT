package konra.hacksito.client.screen;

import konra.hacksito.client.module.WaypointModule;
import konra.hacksito.client.utils.Waypoint;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class WaypointScreen extends Screen {
    private final WaypointModule module;
    private Waypoint targetWaypoint; 
    private TextFieldWidget nameField;
    private TextFieldWidget xField, yField, zField;
    private double hue = 0.5;

    public WaypointScreen(WaypointModule module) {
        super(Text.literal("Creador de Waypoints"));
        this.module = module;
        this.targetWaypoint = null;
    }

    public WaypointScreen(WaypointModule module, Waypoint target) {
        super(Text.literal("Editor de Waypoint"));
        this.module = module;
        this.targetWaypoint = target;
        
        float[] hsb = Color.RGBtoHSB((int)(target.r * 255), (int)(target.g * 255), (int)(target.b * 255), null);
        this.hue = hsb[0];
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        nameField = new TextFieldWidget(textRenderer, centerX - 50, centerY - 60, 100, 16, Text.literal("Nombre"));
        nameField.setMaxLength(20);
        
        xField = new TextFieldWidget(textRenderer, centerX - 50, centerY - 40, 100, 16, Text.literal("X"));
        yField = new TextFieldWidget(textRenderer, centerX - 50, centerY - 20, 100, 16, Text.literal("Y"));
        zField = new TextFieldWidget(textRenderer, centerX - 50, centerY - 0, 100, 16, Text.literal("Z"));

        if (targetWaypoint != null) {
            nameField.setText(targetWaypoint.name);
            xField.setText(String.valueOf((int) targetWaypoint.pos.x));
            yField.setText(String.valueOf((int) targetWaypoint.pos.y));
            zField.setText(String.valueOf((int) targetWaypoint.pos.z));
        } else if (client.player != null) {
            nameField.setText("Mi Punto");
            xField.setText(String.valueOf((int) client.player.getX()));
            yField.setText(String.valueOf((int) client.player.getY()));
            zField.setText(String.valueOf((int) client.player.getZ()));
        }

        this.addSelectableChild(nameField);
        this.addSelectableChild(xField);
        this.addSelectableChild(yField);
        this.addSelectableChild(zField);

        this.addDrawableChild(new SliderWidget(centerX - 50, centerY + 20, 100, 20, Text.literal("Color"), hue) {
            @Override protected void updateMessage() { this.setMessage(Text.literal("Color")); }
            @Override protected void applyValue() { hue = this.value; }
        });

        String textoBoton = (targetWaypoint != null) ? "Guardar Cambios" : "Añadir Punto";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(textoBoton), button -> {
            try {
                String name = nameField.getText();
                double x = Double.parseDouble(xField.getText());
                double y = Double.parseDouble(xField.getText());
                double z = Double.parseDouble(zField.getText());
                double valY = Double.parseDouble(yField.getText()); 

                Color c = Color.getHSBColor((float)hue, 0.8f, 1.0f);

                if (targetWaypoint != null) {
                    targetWaypoint.name = name;
                    targetWaypoint.pos = new Vec3d(x, valY, z);
                    targetWaypoint.r = c.getRed()/255f;
                    targetWaypoint.g = c.getGreen()/255f;
                    targetWaypoint.b = c.getBlue()/255f;
                    
                    if (client.world != null) {
                        targetWaypoint.dimension = client.world.getRegistryKey().getValue().toString();
                    }
                    module.save(); 
                } else {
                    module.addWaypoint(name, new Vec3d(x, valY, z), c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
                }
                
                this.close();
            } catch (Exception e) {
                nameField.setText("¡Error!");
            }
        }).dimensions(centerX - 50, centerY + 45, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.fill(centerX - 60, centerY - 70, centerX + 60, centerY + 75, 0xDD000000);
        context.drawBorder(centerX - 60, centerY - 70, 120, 145, 0xFFFFFFFF);

        Color preview = Color.getHSBColor((float)hue, 0.8f, 1.0f);
        context.fill(centerX + 55, centerY + 20, centerX + 65, centerY + 40, preview.getRGB());

        context.drawTextWithShadow(textRenderer, "Nombre:", centerX - 95, centerY - 56, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "Coords:", centerX - 95, centerY - 36, 0xFFFFFFFF);

        nameField.render(context, mouseX, mouseY, delta);
        xField.render(context, mouseX, mouseY, delta);
        yField.render(context, mouseX, mouseY, delta);
        zField.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}