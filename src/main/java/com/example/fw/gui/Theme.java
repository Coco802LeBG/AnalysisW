package com.example.fw.gui;

import net.minecraft.client.gui.DrawContext;

/** Couleurs ARGB, alpha toujours explicite. */
public final class Theme {
    private Theme() {}

    /** Remplace DrawContext#drawBorder (supprimé en 1.21.6+). */
    public static void border(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x, y, x + w, y + 1, color);
        c.fill(x, y + h - 1, x + w, y + h, color);
        c.fill(x, y + 1, x + 1, y + h - 1, color);
        c.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static final int PANEL_BG        = 0x96000000;
    public static final int HEADER_BG       = 0xE6141414;
    public static final int BORDER          = 0xFF3A3A3A;
    public static final int ACCENT          = 0xFF2ECC71;

    public static final int MODULE_OFF      = 0xA0303030;
    public static final int MODULE_ON       = 0xB02ECC71;
    public static final int HOVER_OVERLAY   = 0x30FFFFFF;
    public static final int SETTINGS_BG     = 0x70000000;

    public static final int TEXT            = 0xFFFFFFFF;
    public static final int TEXT_DIM        = 0xFFB0B0B0;

    public static final int CHECK_BOX_BG    = 0xFF202020;
    public static final int SLIDER_TRACK    = 0xFF202020;
    public static final int SLIDER_FILL     = 0xFF27AE60;

    public static final int DIM_BACKGROUND  = 0x60000000;
}
