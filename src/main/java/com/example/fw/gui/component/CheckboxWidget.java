package com.example.fw.gui.component;

import com.example.fw.gui.Theme;
import com.example.fw.gui.setting.BooleanSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class CheckboxWidget extends SettingComponent {
    private static final int HEIGHT = 14;
    private static final int BOX = 8;
    private final BooleanSetting bool;

    public CheckboxWidget(BooleanSetting setting) {
        super(setting);
        this.bool = setting;
    }

    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void render(DrawContext ctx, TextRenderer font, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY)) ctx.fill(x, y, x + width, y + HEIGHT, Theme.HOVER_OVERLAY);

        ctx.drawText(font, bool.getName(), x + 6, y + (HEIGHT - 8) / 2 + 1, Theme.TEXT, false);

        int bx = x + width - BOX - 6;
        int by = y + (HEIGHT - BOX) / 2;
        ctx.fill(bx, by, bx + BOX, by + BOX, Theme.CHECK_BOX_BG);
        ctx.drawBorder(bx, by, BOX, BOX, Theme.BORDER);
        if (bool.getValue()) ctx.fill(bx + 2, by + 2, bx + BOX - 2, by + BOX - 2, Theme.ACCENT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            bool.toggle();
            return true;
        }
        return false;
    }
}
