package com.example.fw.gui.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean def) { super(name, def); }
    public void toggle() { value = !value; }
    public boolean get() { return value; }
}
