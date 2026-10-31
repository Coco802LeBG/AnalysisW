package com.example.fw;

import com.example.fw.gui.ClickGuiScreen;
import com.example.fw.gui.GuiModule;
import com.example.fw.module.*;
import com.example.fw.render.LineBatch;
import com.example.fw.util.ChunkActivityTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AnalysisSuite implements ClientModInitializer {
    public static final List<Module> MODULES = List.of(
            new StorageEntityRadar(),
            new TargetBlockNotifier(),
            new SusChunkFinder(),
            new HudCleaner()
    );

    @Override
    public void onInitializeClient() {
        MinecraftClient mc = MinecraftClient.getInstance();

        KeyBinding openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fw.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.fw"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                List<GuiModule> gui = List.copyOf(MODULES);
                client.setScreen(new ClickGuiScreen(gui));
            }
            if (client.world == null || client.player == null) return;
            for (Module m : MODULES) if (m.isEnabled()) m.onTick(client);
        });

        WorldRenderEvents.LAST.register(ctx -> {
            if (mc.world == null || mc.player == null || ctx.matrixStack() == null) return;
            LineBatch batch = new LineBatch(ctx.matrixStack(), ctx.camera().getPos());
            for (Module m : MODULES) if (m.isEnabled()) m.onRender(ctx, batch, mc);
            batch.flush();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ChunkActivityTracker.clear();
            for (Module m : MODULES) m.onWorldLeave();
        });
    }
}
