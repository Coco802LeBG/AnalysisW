package com.example.fw.gui.component;

import com.example.fw.gui.GuiModule;
import com.example.fw.gui.Theme;
import com.example.fw.gui.setting.BooleanSetting;
import com.example.fw.gui.setting.NumberSetting;
import com.example.fw.gui.setting.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    public static final int HEIGHT = 14;

    private final GuiModule module;
    private final List<SettingComponent> settings = new ArrayList<>();
    private boolean extended;
    private int x, y, width;

    public ModuleButton(GuiModule module) {
        this.module = module;
        for (Setting<?> s : module.getSettings()) {
            if (s instanceof BooleanSetting b)     settings.add(new CheckboxWidget(b));
            else if (s instanceof NumberSetting n) settings.add(new SliderWidget(n));
        }
    }

    public boolean isExtended() { return extended; }

    public int getTotalHeight() {
        int h = HEIGHT;
        if (extended) for (SettingComponent c : settings) h += c.getHeight();
        return h;
    }

    public void setBounds(int x, int y, int width) {
        this.x = x; this.y = y; this.width = width;
    }

    private boolean isOverButton(double mx, double my) {
        return mx >= x && mx < x + width && my >= y && my < y + HEIGHT;
    }

    public void render(DrawContext ctx, TextRenderer font, int mouseX, int mouseY) {
        ctx.fill(x, y, x + width, y + HEIGHT, module.isEnabled() ? Theme.MODULE_ON : Theme.MODULE_OFF);
        if (isOverButton(mouseX, mouseY)) ctx.fill(x, y, x + width, y + HEIGHT, Theme.HOVER_OVERLAY);

        ctx.drawText(font, module.getName(), x + 5, y + (HEIGHT - 8) / 2 + 1,
                module.isEnabled() ? Theme.TEXT : Theme.TEXT_DIM, false);

        if (!settings.isEmpty()) {
            String ind = extended ? "-" : "+";
            ctx.drawText(font, ind, x + width - 5 - font.getWidth(ind),
                    y + (HEIGHT - 8) / 2 + 1, Theme.TEXT_DIM, false);
        }

        if (extended) {
            int cy = y + HEIGHT;
            int total = getTotalHeight() - HEIGHT;
            ctx.fill(x, cy, x + width, cy + total, Theme.SETTINGS_BG);
            ctx.fill(x, cy, x + 1, cy + total, Theme.ACCENT);

            for (SettingComponent c : settings) {
                c.setBounds(x + 1, cy, width - 1);
                c.render(ctx, font, mouseX, mouseY);
                cy += c.getHeight();
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOverButton(mouseX, mouseY)) {
            if (button == 0) {
                module.toggle();
                return true;
            }
            if (button == 1 && !settings.isEmpty()) {
                extended = !extended;
                return true;
            }
            return true;
        }
        if (extended) {
            for (SettingComponent c : settings) {
                if (c.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (extended) for (SettingComponent c : settings) c.mouseDragged(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (SettingComponent c : settings) c.mouseReleased(mouseX, mouseY, button);
    }
}
