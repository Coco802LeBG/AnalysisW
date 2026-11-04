package com.example.fw.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public final class ChunkUtil {
    private ChunkUtil() {}

    /** Chunks actuellement chargés autour du joueur. */
    public static List<ChunkPos> loadedAroundPlayer(MinecraftClient mc) {
        List<ChunkPos> out = new ArrayList<>();
        if (mc.world == null || mc.player == null) return out;
        int vd = mc.options.getClampedViewDistance();
        ChunkPos c = mc.player.getChunkPos();
        for (int x = c.x - vd; x <= c.x + vd; x++)
            for (int z = c.z - vd; z <= c.z + vd; z++) {
                WorldChunk wc = mc.world.getChunkManager().getWorldChunk(x, z);
                if (wc != null) out.add(wc.getPos());
            }
        return out;
    }
}
