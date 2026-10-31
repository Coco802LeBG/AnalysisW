package com.example.fw.module;

import com.example.fw.gui.Category;
import com.example.fw.gui.setting.BooleanSetting;
import com.example.fw.gui.setting.NumberSetting;
import com.example.fw.render.LineBatch;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class StorageEntityRadar extends Module {
    private static final int RED  = 0xFFFF2020;
    private static final int GOLD = 0xFFFFC107;
    private static final int CYAN = 0xFF40C4FF;

    private final BooleanSetting showPlayers = add(new BooleanSetting("Players", true));
    private final BooleanSetting showMobs    = add(new BooleanSetting("Mobs", false));
    private final BooleanSetting showStorage = add(new BooleanSetting("Storage", true));
    private final NumberSetting  range       = add(new NumberSetting("Range", 128, 16, 256, 8));

    private volatile List<Box> storageBoxes = List.of();

    public StorageEntityRadar() { super("Storage & Entity Radar", Category.RENDER); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!showStorage.get() || mc.world.getTime() % 10 != 0) return;

        ClientWorld world = mc.world;
        int vd = mc.options.getClampedViewDistance();
        ChunkPos center = mc.player.getChunkPos();
        List<Box> out = new ArrayList<>();

        for (int cx = center.x - vd; cx <= center.x + vd; cx++) {
            for (int cz = center.z - vd; cz <= center.z + vd; cz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!isStorage(be)) continue;
                    BlockPos p = be.getPos();
                    var shape = be.getCachedState().getOutlineShape(world, p);
                    out.add(shape.isEmpty() ? new Box(p) : shape.getBoundingBox().offset(p));
                }
            }
        }
        storageBoxes = out;
    }

    private static boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity
            || be instanceof EnderChestBlockEntity
            || be instanceof BarrelBlockEntity
            || be instanceof ShulkerBoxBlockEntity
            || be instanceof HopperBlockEntity;
    }

    @Override
    public void onRender(WorldRenderContext ctx, LineBatch batch, MinecraftClient mc) {
        float td = ctx.tickCounter().getTickDelta(false);

        if (showStorage.get()) for (Box b : storageBoxes) batch.box(b, GOLD);

        double maxSq = range.get() * range.get();
        Vec3d camPos = ctx.camera().getPos();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            boolean isPlayer = e instanceof PlayerEntity;
            if (isPlayer ? !showPlayers.get() : !(showMobs.get() && e instanceof LivingEntity)) continue;
            if (e.squaredDistanceTo(camPos) > maxSq) continue;

            double dx = MathHelper.lerp(td, e.lastRenderX, e.getX()) - e.getX();
            double dy = MathHelper.lerp(td, e.lastRenderY, e.getY()) - e.getY();
            double dz = MathHelper.lerp(td, e.lastRenderZ, e.getZ()) - e.getZ();
            batch.box(e.getBoundingBox().offset(dx, dy, dz), isPlayer ? RED : CYAN);
        }
    }

    @Override public void onWorldLeave() { storageBoxes = List.of(); }
    @Override protected void onDisable(MinecraftClient mc) { storageBoxes = List.of(); }
}
