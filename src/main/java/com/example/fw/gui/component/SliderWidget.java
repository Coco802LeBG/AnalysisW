package com.example.fw.gui.component;

import com.example.fw.gui.Theme;
import com.example.fw.gui.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public class SliderWidget extends SettingComponent {
    private static final int HEIGHT = 18;
    private static final int PAD = 6;
    private final NumberSetting number;
    private boolean dragging;

    public SliderWidget(NumberSetting setting) {
        super(setting);
        this.number = setting;
    }

    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void render(DrawContext ctx, TextRenderer font, int mouseX, int mouseY) {
        if (isHovered(mouseX, mouseY) || dragging)
            ctx.fill(x, y, x + width, y + HEIGHT, Theme.HOVER_OVERLAY);

        String valueText = format(number.getValue());
        ctx.drawText(font, number.getName(), x + PAD, y + 2, Theme.TEXT, false);
        ctx.drawText(font, valueText, x + width - PAD - font.getWidth(valueText), y + 2, Theme.TEXT_DIM, false);

        int trackX1 = x + PAD, trackX2 = x + width - PAD;
        int trackY1 = y + HEIGHT - 6, trackY2 = y + HEIGHT - 3;
        ctx.fill(trackX1, trackY1, trackX2, trackY2, Theme.SLIDER_TRACK);

        double ratio = (number.getValue() - number.getMin()) / (number.getMax() - number.getMin());
        int fillX = trackX1 + (int) ((trackX2 - trackX1) * MathHelper.clamp(ratio, 0, 1));
        ctx.fill(trackX1, trackY1, fillX, trackY2, Theme.SLIDER_FILL);
        ctx.fill(fillX - 1, trackY1 - 1, fillX + 1, trackY2 + 1, Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            updateFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) updateFromMouse(mouseX);
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
    }

    private void updateFromMouse(double mouseX) {
        double left = x + PAD, right = x + width - PAD;
        double ratio = MathHelper.clamp((mouseX - left) / (right - left), 0.0, 1.0);
        number.setValue(number.getMin() + ratio * (number.getMax() - number.getMin()));
    }

    private String format(double v) {
        return number.getStep() >= 1.0 ? String.valueOf((int) Math.round(v)) : String.format("%.2f", v);
    }
}
