package com.example.fw.gui.component;

import com.example.fw.gui.setting.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class SettingComponent {
    protected final Setting<?> setting;
    protected int x, y, width;

    protected SettingComponent(Setting<?> setting) { this.setting = setting; }

    public void setBounds(int x, int y, int width) {
        this.x = x; this.y = y; this.width = width;
    }

    public abstract int getHeight();
    public abstract void render(DrawContext ctx, TextRenderer font, int mouseX, int mouseY);

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public void mouseDragged(double mouseX, double mouseY, int button) {}
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + getHeight();
    }
}
