package com.example.fw;

import com.example.fw.gui.ClickGuiScreen;
import com.example.fw.gui.GuiModule;
import com.example.fw.module.*;
import com.example.fw.module.Module; // lève l'ambiguïté avec java.lang.Module
import com.example.fw.render.LineBatch;
import com.example.fw.util.ChunkActivityTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AnalysisSuite implements ClientModInitializer {
    public static final List<Module> MODULES = List.of(
            new StorageEntityRadar(),
            new TargetBlockNotifier(),
            new SusChunkFinder(),
            new HudCleaner()
    );

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("fw", "main"));

    @Override
    public void onInitializeClient() {
        KeyBinding openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fw.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                List<GuiModule> gui = List.copyOf(MODULES);
                client.setScreen(new ClickGuiScreen(gui));
            }
            if (client.world == null || client.player == null) return;
            for (Module m : MODULES) if (m.isEnabled()) m.onTick(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ChunkActivityTracker.clear();
            for (Module m : MODULES) m.onWorldLeave();
        });
    }

    /** Appelé par InGameHudMixin à la fin du rendu du HUD. */
    public static void renderOverlay(DrawContext ctx, float tickProgress) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        LineBatch batch = new LineBatch(ctx, mc, tickProgress);
        for (Module m : MODULES) if (m.isEnabled()) m.onRender(batch, mc, tickProgress);
    }
}
