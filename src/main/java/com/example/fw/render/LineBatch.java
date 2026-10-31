package com.example.fw.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Accumule toutes les arêtes de la frame et les dessine en un seul draw call. */
public final class LineBatch {
    private final BufferBuilder buf;
    private final Matrix4f mat;
    private final Vec3d cam;
    private boolean empty = true;

    public LineBatch(MatrixStack matrices, Vec3d camPos) {
        this.cam = camPos;
        this.mat = matrices.peek().getPositionMatrix();
        this.buf = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
    }

    public void box(Box b, int argb) {
        float x1 = (float) (b.minX - cam.x), y1 = (float) (b.minY - cam.y), z1 = (float) (b.minZ - cam.z);
        float x2 = (float) (b.maxX - cam.x), y2 = (float) (b.maxY - cam.y), z2 = (float) (b.maxZ - cam.z);
        empty = false;

        line(x1,y1,z1, x2,y1,z1, argb); line(x2,y1,z1, x2,y1,z2, argb);
        line(x2,y1,z2, x1,y1,z2, argb); line(x1,y1,z2, x1,y1,z1, argb);

        line(x1,y2,z1, x2,y2,z1, argb); line(x2,y2,z1, x2,y2,z2, argb);
        line(x2,y2,z2, x1,y2,z2, argb); line(x1,y2,z2, x1,y2,z1, argb);

        line(x1,y1,z1, x1,y2,z1, argb); line(x2,y1,z1, x2,y2,z1, argb);
        line(x2,y1,z2, x2,y2,z2, argb); line(x1,y1,z2, x1,y2,z2, argb);
    }

    private void line(float x1, float y1, float z1, float x2, float y2, float z2, int argb) {
        buf.vertex(mat, x1, y1, z1).color(argb);
        buf.vertex(mat, x2, y2, z2).color(argb);
    }

    public void flush() {
        if (empty) return;
        BuiltBuffer built = buf.endNullable();
        if (built == null) return;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0f);

        BufferRenderer.drawWithGlobalProgram(built);

        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
