package konra.hacksito.client.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.systems.RenderSystem;
import konra.hacksito.client.utils.RenderUtils;
import konra.hacksito.client.utils.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WaypointModule extends Module {
    private List<Waypoint> waypoints = new ArrayList<>();
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/font/ascii.png");
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public WaypointModule() {
        super("Waypoints");
        this.enabled = true;
        this.configFile = new File(MinecraftClient.getInstance().runDirectory, "hacksito_waypoints.json");
        load();
    }

    // --- PERSISTENCIA PROFESIONAL ---
    public List<Waypoint> getWaypoints() { return waypoints; }
    
    public void save() {
        try (Writer writer = new FileWriter(configFile)) {
            gson.toJson(waypoints, writer);
        } catch (IOException e) {
            System.err.println("[Hacksito] Error saving waypoints: " + e.getMessage());
        }
    }

    public void load() {
        if (!configFile.exists()) return;
        try (Reader reader = new FileReader(configFile)) {
            List<Waypoint> loaded = gson.fromJson(reader, new TypeToken<List<Waypoint>>(){}.getType());
            if (loaded != null) this.waypoints = loaded;
        } catch (IOException e) {
            System.err.println("[Hacksito] Error loading waypoints: " + e.getMessage());
        }
    }

    public void addWaypoint(String name, Vec3d pos, float r, float g, float b) {
        if (MinecraftClient.getInstance().world != null) {
            String currentDim = MinecraftClient.getInstance().world.getRegistryKey().getValue().toString();
            this.waypoints.add(new Waypoint(name, pos, r, g, b, currentDim));
            save(); 
        }
    }

    // --- RENDERIZADO CORE ---
    public void render(MatrixStack matrices, double camX, double camY, double camZ) {
        if (waypoints.isEmpty()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        
        String currentDim = client.world.getRegistryKey().getValue().toString();

        for (Waypoint wp : waypoints) {
            if (wp.dimension == null || !wp.dimension.equals(currentDim)) continue;

            // 1. CÁLCULO DE POSICIONES Y PROYECCIÓN (INFINITO)
            double realX = wp.pos.x - camX;
            double realY = wp.pos.y - camY;
            double realZ = wp.pos.z - camZ;
            double dist = Math.sqrt(realX * realX + realY * realY + realZ * realZ);

            double renderX = realX;
            double renderY = realY;
            double renderZ = realZ;

            // Si está muy lejos (ej. > 200m), proyectamos a 200m para evitar el culling de Minecraft
            if (dist > 200) {
                double multiplier = 200 / dist;
                renderX *= multiplier;
                renderY *= multiplier;
                renderZ *= multiplier;
            }

            // 2. ELEMENTOS VISUALES
            renderGradientLine(matrices, renderX, renderY, renderZ, wp.r, wp.g, wp.b);
            renderWaypointName(client, client.textRenderer, matrices, wp, renderX, renderY, renderZ, dist);
        }
    }

    private void renderGradientLine(MatrixStack matrices, double x, double y, double z, float r, float g, float b) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        // Efecto Beacon: La línea sube y se desvanece suavemente
        for (int i = 0; i < 60; i += 10) {
            float alphaTop = Math.max(0, 0.4f - (i / 60f));
            float alphaBottom = 0.4f - ((i - 10 < 0 ? 0 : i - 10) / 60f);
            
            buffer.vertex(matrix, (float)x, (float)y + i, (float)z).color(r, g, b, alphaBottom).next();
            buffer.vertex(matrix, (float)x, (float)y + i + 10, (float)z).color(r, g, b, alphaTop).next();
        }
        
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderWaypointName(MinecraftClient client, TextRenderer textRenderer, MatrixStack matrices, Waypoint wp, double x, double y, double z, double dist) {
        matrices.push();
        
        // 3. OFFSET VERTICAL (15 bloques arriba del punto de creación)
        matrices.translate(x, y + 15.0, z); 
        
        // Rotación hacia la cámara
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-client.gameRenderer.getCamera().getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.gameRenderer.getCamera().getPitch()));
        
        // 4. ESCALA INTELIGENTE (CLAMPING)
        // La escala crece con la distancia, pero tiene un techo para no ser gigante
        float scale = (float) (dist * 0.01f);
        scale = Math.max(0.025f, Math.min(scale, 0.35f)); // Min 0.025, Max 0.35
        
        matrices.scale(-scale, -scale, scale);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        
        RenderSystem.disableDepthTest(); // Para que se vea a través de paredes
        
        String nameTag = "§l" + wp.name.toUpperCase();
        String distTag = " §7[" + (int)dist + "m]";
        float totalWidth = textRenderer.getWidth(nameTag + distTag);
        
        // FONDO (Moderno y semi-transparente)
        VertexConsumer guiBuffer = immediate.getBuffer(RenderLayer.getGui());
        drawInternalRect(matrix, guiBuffer, -(totalWidth/2f)-4, -2, (totalWidth/2f)+4, 11, 0x88000000);
        
        // BARRA DE ACENTO (Color del waypoint)
        int accentColor = (0xFF << 24) | ((int)(wp.r * 255) << 16) | ((int)(wp.g * 255) << 8) | (int)(wp.b * 255);
        drawInternalRect(matrix, guiBuffer, -(totalWidth/2f)-4, 10, (totalWidth/2f)+4, 12, accentColor);
        
        // DIBUJADO DE TEXTO
        textRenderer.draw(nameTag, -totalWidth/2f, 0, accentColor, false, matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
        textRenderer.draw(distTag, (-totalWidth/2f) + textRenderer.getWidth(nameTag), 0, 0xFFFFFF, false, matrix, immediate, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);
        
        immediate.draw(); 
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    private void drawInternalRect(Matrix4f matrix, VertexConsumer buffer, float x1, float y1, float x2, float y2, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        buffer.vertex(matrix, x1, y2, 0.0f).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, 0.0f).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, 0.0f).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, 0.0f).color(r, g, b, a).next();
    }

    @Override public void onEnable() {}
    @Override public void onDisable() {}
    @Override public void onTick() {}
}