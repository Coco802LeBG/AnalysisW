package com.example.fw.gui;

public enum Category {
    RENDER("Render"), MOVEMENT("Movement"), PLAYER("Player"), MISC("Misc");

    public final String label;
    Category(String label) { this.label = label; }
}
