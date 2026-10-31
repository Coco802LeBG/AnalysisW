package com.example.fw.module;

import com.example.fw.gui.Category;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class HudCleaner extends Module {
    /** Lu par InGameHudMixin. */
    public static boolean hideScoreboard = false;
    private static final int GRACE_TICKS = 40;

    private final Map<UUID, String> lastTab = new HashMap<>();
    private final Map<UUID, Integer> ghostTicks = new HashMap<>();
    private final Set<UUID> alerted = new HashSet<>();

    public HudCleaner() { super("HUD Cleaner / Vanish", Category.MISC); }

    @Override protected void onEnable(MinecraftClient mc)  { hideScoreboard = true; }
    @Override protected void onDisable(MinecraftClient mc) { hideScoreboard = false; reset(); }
    @Override public void onWorldLeave() { reset(); }

    private void reset() { lastTab.clear(); ghostTicks.clear(); alerted.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        var handler = mc.getNetworkHandler();
        if (handler == null) return;

        Map<UUID, String> tab = new HashMap<>();
        for (PlayerListEntry e : handler.getPlayerList())
            tab.put(e.getProfile().getId(), e.getProfile().getName());

        Set<UUID> ghostsNow = new HashSet<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            UUID id = p.getUuid();
            if (p == mc.player || tab.containsKey(id)) continue;
            if (id.version() == 2) continue; // NPC de plugins
            ghostsNow.add(id);
            if (ghostTicks.merge(id, 1, Integer::sum) >= GRACE_TICKS && alerted.add(id))
                warn(mc, p.getGameProfile().getName(), "visible dans le monde mais absent de la TabList");
        }
        ghostTicks.keySet().retainAll(ghostsNow);

        for (var old : lastTab.entrySet()) {
            UUID id = old.getKey();
            if (!tab.containsKey(id) && mc.world.getPlayerByUuid(id) != null && alerted.add(id))
                warn(mc, old.getValue(), "retiré de la TabList mais entité toujours présente");
        }

        alerted.removeIf(tab::containsKey);
        lastTab.clear();
        lastTab.putAll(tab);
    }

    private void warn(MinecraftClient mc, String name, String why) {
        mc.player.sendMessage(Text.literal("[Vanish?] ").formatted(Formatting.RED)
                .append(Text.literal(name + " : " + why).formatted(Formatting.GRAY)), false);
    }
}
