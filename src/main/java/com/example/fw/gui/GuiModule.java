package com.example.fw.gui;

import com.example.fw.gui.setting.Setting;
import java.util.List;

public interface GuiModule {
    String getName();
    Category getCategory();
    boolean isEnabled();
    void toggle();
    default List<Setting<?>> getSettings() { return List.of(); }
}
