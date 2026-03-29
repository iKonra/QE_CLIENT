package konra.hacksito.client.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class RenderUtils {

    public static void drawWireBox(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        setupRender();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        writeWireBoxLines(matrices, buffer, x1, y1, z1, x2, y2, z2, r, g, b, a);
        
        tessellator.draw();
        endRender();
    }

    public static void writeWireBoxLines(MatrixStack matrices, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        float xmin = (float)x1, ymin = (float)y1, zmin = (float)z1;
        float xmax = (float)x2, ymax = (float)y2, zmax = (float)z2;

        line(m, buffer, xmin, ymin, zmin, xmax, ymin, zmin, r, g, b, a);
        line(m, buffer, xmax, ymin, zmin, xmax, ymin, zmax, r, g, b, a);
        line(m, buffer, xmax, ymin, zmax, xmin, ymin, zmax, r, g, b, a);
        line(m, buffer, xmin, ymin, zmax, xmin, ymin, zmin, r, g, b, a);

        line(m, buffer, xmin, ymax, zmin, xmax, ymax, zmin, r, g, b, a);
        line(m, buffer, xmax, ymax, zmin, xmax, ymax, zmax, r, g, b, a);
        line(m, buffer, xmax, ymax, zmax, xmin, ymax, zmax, r, g, b, a);
        line(m, buffer, xmin, ymax, zmax, xmin, ymax, zmin, r, g, b, a);

        line(m, buffer, xmin, ymin, zmin, xmin, ymax, zmin, r, g, b, a);
        line(m, buffer, xmax, ymin, zmin, xmax, ymax, zmin, r, g, b, a);
        line(m, buffer, xmax, ymin, zmax, xmax, ymax, zmax, r, g, b, a);
        line(m, buffer, xmin, ymin, zmax, xmin, ymax, zmax, r, g, b, a);
    }

    public static void drawFilledBox(MatrixStack matrices, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float xmin = (float)x1, ymin = (float)y1, zmin = (float)z1;
        float xmax = (float)x2, ymax = (float)y2, zmax = (float)z2;

        // Norte
        vertex(matrix, buffer, xmin, ymin, zmin, r, g, b, a);
        vertex(matrix, buffer, xmin, ymax, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymin, zmin, r, g, b, a);
        // Sur
        vertex(matrix, buffer, xmin, ymin, zmax, r, g, b, a);
        vertex(matrix, buffer, xmax, ymin, zmax, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmax, r, g, b, a);
        vertex(matrix, buffer, xmin, ymax, zmax, r, g, b, a);
        // Abajo
        vertex(matrix, buffer, xmin, ymin, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymin, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymin, zmax, r, g, b, a);
        vertex(matrix, buffer, xmin, ymin, zmax, r, g, b, a);
        // Arriba
        vertex(matrix, buffer, xmin, ymax, zmin, r, g, b, a);
        vertex(matrix, buffer, xmin, ymax, zmax, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmax, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmin, r, g, b, a);
        // Oeste
        vertex(matrix, buffer, xmin, ymin, zmin, r, g, b, a);
        vertex(matrix, buffer, xmin, ymin, zmax, r, g, b, a);
        vertex(matrix, buffer, xmin, ymax, zmax, r, g, b, a);
        vertex(matrix, buffer, xmin, ymax, zmin, r, g, b, a);
        // Este
        vertex(matrix, buffer, xmax, ymin, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmin, r, g, b, a);
        vertex(matrix, buffer, xmax, ymax, zmax, r, g, b, a);
        vertex(matrix, buffer, xmax, ymin, zmax, r, g, b, a);

        tessellator.draw();
        endRender();
    }

    public static void drawLine(MatrixStack matrices, Vec3d start, Vec3d end, float r, float g, float b, float a) {
        setupRender();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        line(matrix, buffer, (float)start.x, (float)start.y, (float)start.z, (float)end.x, (float)end.y, (float)end.z, r, g, b, a);

        tessellator.draw();
        endRender();
    }

    private static void line(Matrix4f matrix, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        vertex(matrix, buffer, x1, y1, z1, r, g, b, a);
        vertex(matrix, buffer, x2, y2, z2, r, g, b, a);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).normal(0f, 1f, 0f).next();
    }

    private static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); 
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void endRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}