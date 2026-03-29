package konra.hacksito.client.utils;

import net.minecraft.util.math.Vec3d;

public class Waypoint {
    public String name;
    public Vec3d pos;
    public float r, g, b;
    public String dimension; // <--- NUEVO: Para filtrar por mundo

    public Waypoint(String name, Vec3d pos, float r, float g, float b, String dimension) {
        this.name = name;
        this.pos = pos;
        this.r = r;
        this.g = g;
        this.b = b;
        this.dimension = dimension;
    }

    public Waypoint() {}
}