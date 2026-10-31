package com.example.fw.util;

import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ChunkActivityTracker {
    private static final Map<ChunkPos, Integer> UPDATES = new ConcurrentHashMap<>();
    private static final List<Consumer<ChunkPos>> LISTENERS = new CopyOnWriteArrayList<>();

    private ChunkActivityTracker() {}

    public static void addListener(Consumer<ChunkPos> l) { LISTENERS.add(l); }

    public static void record(ChunkPos cp) {
        UPDATES.merge(cp, 1, Integer::sum);
        for (Consumer<ChunkPos> l : LISTENERS) l.accept(cp);
    }

    public static int updatesFor(ChunkPos cp) { return UPDATES.getOrDefault(cp, 0); }
    public static void clear() { UPDATES.clear(); }
}
