package com.example.fw.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Projette des boîtes 3D sur l'écran et les trace en 2D avec DrawContext.
 * (Le pipeline de rendu 3D a été réécrit en 1.21.6+ : RenderSystem.enableBlend, lineWidth,
 * BufferRenderer, getPositionColorProgram, etc. n'existent plus.)
 * Toujours visible à travers les murs. Prévu pour la vue à la première personne.
 */
public final class LineBatch {
    private static final double NEAR = 0.05;

    private final DrawContext ctx;
    private final Vec3d cam;
    private final double rx, ry, rz;   // droite
    private final double ux, uy, uz;   // haut
    private final double fx, fy, fz;   // avant
    private final double focal, cx, cy;
    private final int screenW, screenH;
    private final double[][] c = new double[8][3];

    public LineBatch(DrawContext ctx, MinecraftClient mc, float tickProgress) {
        this.ctx = ctx;
        ClientPlayerEntity p = mc.player;

        Vec3d pos = p.getLerpedPos(tickProgress);
        this.cam = new Vec3d(pos.x, pos.y + (p.getEyeY() - p.getY()), pos.z);

        double yaw = Math.toRadians(p.getYaw(tickProgress));
        double pitch = Math.toRadians(p.getPitch(tickProgress));
        double cp = Math.cos(pitch), sp = Math.sin(pitch);
        double sy = Math.sin(yaw), cy2 = Math.cos(yaw);

        fx = -sy * cp; fy = -sp; fz = cy2 * cp;
        rx = -cy2;     ry = 0;   rz = -sy;
        // up = right x forward
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;

        this.screenW = mc.getWindow().getScaledWidth();
        this.screenH = mc.getWindow().getScaledHeight();
        double fov = Math.toRadians(mc.options.getFov().getValue());
        this.focal = (screenH / 2.0) / Math.tan(fov / 2.0);
        this.cx = screenW / 2.0;
        this.cy = screenH / 2.0;
    }

    public Vec3d cam() { return cam; }

    public void box(Box b, int argb) {
        boolean anyFront = false;
        for (int i = 0; i < 8; i++) {
            double wx = (i & 1) == 0 ? b.minX : b.maxX;
            double wy = (i & 2) == 0 ? b.minY : b.maxY;
            double wz = (i & 4) == 0 ? b.minZ : b.maxZ;
            double dx = wx - cam.x, dy = wy - cam.y, dz = wz - cam.z;
            c[i][0] = dx * rx + dy * ry + dz * rz;
            c[i][1] = dx * ux + dy * uy + dz * uz;
            c[i][2] = dx * fx + dy * fy + dz * fz;
            if (c[i][2] >= NEAR) anyFront = true;
        }
        if (!anyFront) return;

        for (int i = 0; i < 8; i++) {
            if ((i & 1) == 0) line(c[i], c[i | 1], argb);
            if ((i & 2) == 0) line(c[i], c[i | 2], argb);
            if ((i & 4) == 0) line(c[i], c[i | 4], argb);
        }
    }

    private void line(double[] a, double[] b, int argb) {
        double ax = a[0], ay = a[1], az = a[2];
        double bx = b[0], by = b[1], bz = b[2];
        if (az < NEAR && bz < NEAR) return;

        if (az < NEAR) {
            double t = (NEAR - az) / (bz - az);
            ax += (bx - ax) * t; ay += (by - ay) * t; az = NEAR;
        } else if (bz < NEAR) {
            double t = (NEAR - bz) / (az - bz);
            bx += (ax - bx) * t; by += (ay - by) * t; bz = NEAR;
        }

        double x1 = cx + ax / az * focal, y1 = cy - ay / az * focal;
        double x2 = cx + bx / bz * focal, y2 = cy - by / bz * focal;

        if ((x1 < 0 && x2 < 0) || (x1 > screenW && x2 > screenW)
         || (y1 < 0 && y2 < 0) || (y1 > screenH && y2 > screenH)) return;

        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len < 0.5 || len > 20000) return;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate((float) x1, (float) y1);
        m.rotate((float) Math.atan2(dy, dx));
        ctx.fill(0, 0, (int) Math.ceil(len), 1, argb);
        m.popMatrix();
    }
}
