package com.example.fw.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

/** Dessine des arêtes de boîtes à travers les blocs (pas de test de profondeur). */
public final class LineBatch {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
                    .withLocation(Identifier.of("fw", "pipeline/lines_seethrough"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .build());

    public static final RenderLayer LAYER = RenderLayer.of("fw_lines", RenderSetup.builder(PIPELINE).build());

    private static final float WIDTH = 2.0f;

    private final MatrixStack matrices;
    private final Vec3d cam;
    private final VertexConsumer vc;

    public LineBatch(MatrixStack matrices, Vec3d camPos, VertexConsumer vc) {
        this.matrices = matrices;
        this.cam = camPos;
        this.vc = vc;
    }

    public void box(Box b, int argb) {
        VertexRendering.drawOutline(matrices, vc, VoxelShapes.cuboid(b), -cam.x, -cam.y, -cam.z, argb, WIDTH);
    }
}
