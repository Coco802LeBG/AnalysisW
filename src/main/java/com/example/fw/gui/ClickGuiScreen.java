package com.example.fw.gui;

import com.example.fw.gui.component.CategoryPanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class ClickGuiScreen extends Screen {
    private static final Map<Category, int[]> SAVED_POS = new EnumMap<>(Category.class);

    private final List<GuiModule> allModules;
    /** Dernier élément = premier plan. */
    private final List<CategoryPanel> panels = new ArrayList<>();

    public ClickGuiScreen(List<GuiModule> modules) {
        super(Text.literal("ClickGUI"));
        this.allModules = modules;
    }

    @Override
    protected void init() {
        panels.clear();
        int col = 0;
        for (Category cat : Category.values()) {
            List<GuiModule> inCat = allModules.stream().filter(m -> m.getCategory() == cat).toList();
            if (inCat.isEmpty()) continue;

            int[] pos = SAVED_POS.get(cat);
            int px = pos != null ? pos[0] : 12 + col * (CategoryPanel.WIDTH + 8);
            int py = pos != null ? pos[1] : 12;
            panels.add(new CategoryPanel(cat, inCat, px, py));
            col++;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, Theme.DIM_BACKGROUND);
        for (CategoryPanel p : panels) p.render(context, textRenderer, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel p = panels.get(i);
            if (p.mouseClicked(mouseX, mouseY, button)) {
                panels.remove(i);
                panels.add(p);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (CategoryPanel p : panels) p.mouseDragged(mouseX, mouseY, button, width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel p : panels) p.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        for (CategoryPanel p : panels) SAVED_POS.put(p.getCategory(), new int[]{p.getX(), p.getY()});
    }

    @Override public boolean shouldPause() { return false; }
}
