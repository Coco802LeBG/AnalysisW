package com.example.fw.module;

import com.example.fw.gui.Category;
import com.example.fw.gui.GuiModule;
import com.example.fw.gui.setting.Setting;
import com.example.fw.render.LineBatch;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module implements GuiModule {
    private final String name;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    @Override public String getName() { return name; }
    @Override public Category getCategory() { return category; }
    @Override public boolean isEnabled() { return enabled; }
    @Override public List<Setting<?>> getSettings() { return settings; }
    @Override public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (value) onEnable(mc); else onDisable(mc);
    }

    protected void onEnable(MinecraftClient mc) {}
    protected void onDisable(MinecraftClient mc) {}
    public void onTick(MinecraftClient mc) {}
    public void onRender(WorldRenderContext ctx, LineBatch batch, MinecraftClient mc) {}
    /** Appelé à la déconnexion : vider les caches. */
    public void onWorldLeave() {}
}
