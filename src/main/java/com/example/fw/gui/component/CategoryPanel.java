package com.example.fw.gui.component;

import com.example.fw.gui.Category;
import com.example.fw.gui.GuiModule;
import com.example.fw.gui.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {
    public static final int WIDTH = 110;
    public static final int HEADER_HEIGHT = 16;

    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();

    private int x, y;
    private boolean open = true;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    public CategoryPanel(Category category, List<GuiModule> modules, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;
        for (GuiModule m : modules) buttons.add(new ModuleButton(m));
    }

    public Category getCategory() { return category; }
    public int getX() { return x; }
    public int getY() { return y; }

    private int bodyHeight() {
        int h = 0;
        for (ModuleButton b : buttons) h += b.getTotalHeight();
        return h;
    }

    private boolean isOverHeader(double mx, double my) {
        return mx >= x && mx < x + WIDTH && my >= y && my < y + HEADER_HEIGHT;
    }

    public boolean isOver(double mx, double my) {
        int h = HEADER_HEIGHT + (open ? bodyHeight() : 0);
        return mx >= x && mx < x + WIDTH && my >= y && my < y + h;
    }

    public void render(DrawContext ctx, TextRenderer font, int mouseX, int mouseY) {
        int bodyH = open ? bodyHeight() : 0;
        int totalH = HEADER_HEIGHT + bodyH;

        ctx.fill(x, y, x + WIDTH, y + totalH, Theme.PANEL_BG);

        ctx.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, Theme.HEADER_BG);
        ctx.fill(x, y + HEADER_HEIGHT - 1, x + WIDTH, y + HEADER_HEIGHT, Theme.ACCENT);
        ctx.drawText(font, category.label, x + 6, y + (HEADER_HEIGHT - 8) / 2, Theme.TEXT, true);
        String arrow = open ? "v" : ">";
        ctx.drawText(font, arrow, x + WIDTH - 6 - font.getWidth(arrow),
                y + (HEADER_HEIGHT - 8) / 2, Theme.TEXT_DIM, false);

        if (open) {
            int cy = y + HEADER_HEIGHT;
            for (ModuleButton b : buttons) {
                b.setBounds(x, cy, WIDTH);
                b.render(ctx, font, mouseX, mouseY);
                cy += b.getTotalHeight();
            }
        }

        ctx.drawBorder(x, y, WIDTH, totalH, Theme.BORDER);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isOver(mouseX, mouseY)) return false;

        if (isOverHeader(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
            } else if (button == 1) {
                open = !open;
            }
            return true;
        }

        if (open) {
            for (ModuleButton b : buttons) {
                if (b.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return true;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, int screenW, int screenH) {
        if (dragging && button == 0) {
            x = MathHelper.clamp((int) mouseX - dragOffsetX, 0, Math.max(0, screenW - WIDTH));
            y = MathHelper.clamp((int) mouseY - dragOffsetY, 0, Math.max(0, screenH - HEADER_HEIGHT));
        }
        if (open) for (ModuleButton b : buttons) b.mouseDragged(mouseX, mouseY, button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
        for (ModuleButton b : buttons) b.mouseReleased(mouseX, mouseY, button);
    }
}
