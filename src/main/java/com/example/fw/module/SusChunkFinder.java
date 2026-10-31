package com.example.fw.module;

import com.example.fw.gui.Category;
import com.example.fw.gui.setting.NumberSetting;
import com.example.fw.render.LineBatch;
import com.example.fw.util.ChunkActivityTracker;
import com.example.fw.util.ChunkUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SusChunkFinder extends Module {
    private static final Set<Block> NATURAL_EMITTERS = Set.of(
            Blocks.LAVA, Blocks.MAGMA_BLOCK, Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.GLOWSTONE,
            Blocks.SHROOMLIGHT, Blocks.GLOW_LICHEN, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
            Blocks.SEA_LANTERN, Blocks.CRYING_OBSIDIAN, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME,
            Blocks.LAVA_CAULDRON, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.BROWN_MUSHROOM, Blocks.SCULK_CATALYST, Blocks.OCHRE_FROGLIGHT,
            Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.AMETHYST_CLUSTER,
            Blocks.LARGE_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD, Blocks.SMALL_AMETHYST_BUD);

    private final NumberSetting threshold = add(new NumberSetting("Threshold", 8, 2, 60, 1));

    private final Map<ChunkPos, Integer> scores = new ConcurrentHashMap<>();
    private final Set<ChunkPos> queue = new LinkedHashSet<>();

    public SusChunkFinder() {
        super("Sus Chunk Finder", Category.RENDER);
        ClientChunkEvents.CHUNK_LOAD.register((w, c) -> { if (isEnabled()) queue.add(c.getPos()); });
        ClientChunkEvents.CHUNK_UNLOAD.register((w, c) -> { queue.remove(c.getPos()); scores.remove(c.getPos()); });
        ChunkActivityTracker.addListener(cp -> { if (isEnabled()) queue.add(cp); });
    }

    @Override protected void onEnable(MinecraftClient mc) { queue.addAll(ChunkUtil.loadedAroundPlayer(mc)); }
    @Override protected void onDisable(MinecraftClient mc) { queue.clear(); scores.clear(); }
    @Override public void onWorldLeave() { queue.clear(); scores.clear(); }

    private static boolean isHumanMadeLight(BlockState s) {
        return s.getLuminance() > 0 && !NATURAL_EMITTERS.contains(s.getBlock());
    }

    private static boolean isHumanMadeBlock(BlockState s) {
        return s.isOf(Blocks.CRAFTING_TABLE) || s.isOf(Blocks.FURNACE) || s.isOf(Blocks.CHEST)
            || s.isOf(Blocks.BARREL) || s.isIn(BlockTags.BEDS) || s.isIn(BlockTags.DOORS)
            || s.isOf(Blocks.GLASS) || s.isOf(Blocks.GLASS_PANE) || s.isIn(BlockTags.WOOL);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world.getRegistryKey() == World.NETHER) return;
        Iterator<ChunkPos> it = queue.iterator();
        if (!it.hasNext()) return;
        ChunkPos cp = it.next();
        it.remove();
        WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cp.x, cp.z);
        if (chunk == null) return;

        int lights = 0, made = 0;
        for (ChunkSection s : chunk.getSectionArray()) {
            if (s == null || s.isEmpty()) continue;
            if (!s.hasAny(st -> isHumanMadeLight(st) || isHumanMadeBlock(st))) continue;
            for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) {
                BlockState st = s.getBlockState(x, y, z);
                if (isHumanMadeLight(st)) lights++;
                else if (isHumanMadeBlock(st)) made++;
            }
        }
        int score = lights * 2 + made + ChunkActivityTracker.updatesFor(cp) * 4;
        if (score >= threshold.getInt()) scores.put(cp, score); else scores.remove(cp);
    }

    @Override
    public void onRender(WorldRenderContext ctx, LineBatch batch, MinecraftClient mc) {
        double py = mc.player.getY();
        for (var e : scores.entrySet()) {
            ChunkPos cp = e.getKey();
            float t = Math.min(1f, e.getValue() / 60f);
            int color = 0xFF000000 | 0xFF0000 | ((int) (255 * (1 - t)) << 8);
            batch.box(new Box(cp.getStartX(), py - 2, cp.getStartZ(),
                              cp.getStartX() + 16, py + 2, cp.getStartZ() + 16), color);
        }
    }
}
