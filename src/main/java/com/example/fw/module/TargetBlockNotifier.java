package com.example.fw.module;

import com.example.fw.gui.Category;
import com.example.fw.render.LineBatch;
import com.example.fw.util.ChunkActivityTracker;
import com.example.fw.util.ChunkUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TargetBlockNotifier extends Module {
    private static final Set<Block> TARGETS = Set.of(
            Blocks.NETHERITE_BLOCK, Blocks.SPAWNER,
            Blocks.SUSPICIOUS_GRAVEL, Blocks.SUSPICIOUS_SAND);
    private static final int COLOR = 0xFFB040FF;
    private static final int CHUNKS_PER_TICK = 2;
    private static final int MAX_RENDERED = 512;

    private final Map<ChunkPos, List<BlockPos>> found = new ConcurrentHashMap<>();
    private final Set<BlockPos> notified = ConcurrentHashMap.newKeySet();
    private final Set<ChunkPos> queue = new LinkedHashSet<>();

    public TargetBlockNotifier() {
        super("Target Block Notifier", Category.RENDER);
        ClientChunkEvents.CHUNK_LOAD.register((w, c) -> { if (isEnabled()) queue.add(c.getPos()); });
        ClientChunkEvents.CHUNK_UNLOAD.register((w, c) -> {
            queue.remove(c.getPos());
            List<BlockPos> old = found.remove(c.getPos());
            if (old != null) notified.removeAll(old);
        });
        ChunkActivityTracker.addListener(cp -> { if (isEnabled()) queue.add(cp); });
    }

    @Override
    protected void onEnable(MinecraftClient mc) { queue.addAll(ChunkUtil.loadedAroundPlayer(mc)); }

    @Override
    protected void onDisable(MinecraftClient mc) { queue.clear(); found.clear(); notified.clear(); }

    @Override
    public void onWorldLeave() { queue.clear(); found.clear(); notified.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        Iterator<ChunkPos> it = queue.iterator();
        for (int i = 0; i < CHUNKS_PER_TICK && it.hasNext(); i++) {
            ChunkPos cp = it.next();
            it.remove();
            WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cp.x, cp.z);
            if (chunk != null) scan(mc, chunk);
        }
    }

    private void scan(MinecraftClient mc, WorldChunk chunk) {
        List<BlockPos> hits = new ArrayList<>();
        ChunkSection[] sections = chunk.getSectionArray();

        for (int i = 0; i < sections.length; i++) {
            ChunkSection s = sections[i];
            if (s == null || s.isEmpty() || !s.hasAny(st -> TARGETS.contains(st.getBlock()))) continue;

            int baseY = chunk.sectionIndexToCoord(i) << 4;
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 16; y++)
                    for (int z = 0; z < 16; z++) {
                        if (TARGETS.contains(s.getBlockState(x, y, z).getBlock())) {
                            hits.add(new BlockPos(chunk.getPos().getStartX() + x, baseY + y,
                                                  chunk.getPos().getStartZ() + z));
                        }
                    }
        }

        if (hits.isEmpty()) { found.remove(chunk.getPos()); return; }
        found.put(chunk.getPos(), hits);

        for (BlockPos p : hits) {
            if (!notified.add(p)) continue;
            Block b = chunk.getBlockState(p).getBlock();
            mc.player.sendMessage(Text.literal("[Target] ").formatted(Formatting.DARK_GRAY)
                    .append(Text.translatable(b.getTranslationKey()).formatted(Formatting.LIGHT_PURPLE))
                    .append(Text.literal(" @ " + p.getX() + " " + p.getY() + " " + p.getZ())
                            .formatted(Formatting.GRAY)), false);
        }
    }

    @Override
    public void onRender(WorldRenderContext ctx, LineBatch batch, MinecraftClient mc) {
        int n = 0;
        for (List<BlockPos> list : found.values())
            for (BlockPos p : list) {
                if (++n > MAX_RENDERED) return;
                batch.box(new Box(p), COLOR);
            }
    }
}
