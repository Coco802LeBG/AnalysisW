package com.example.fw.gui.setting;

public class NumberSetting extends Setting<Double> {
    private final double min, max, step;

    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name, def);
        this.min = min; this.max = max; this.step = step;
    }

    public double getMin()  { return min; }
    public double getMax()  { return max; }
    public double getStep() { return step; }
    public double get()     { return value; }
    public int getInt()     { return (int) Math.round(value); }

    @Override
    public void setValue(Double v) {
        double snapped = Math.round((v - min) / step) * step + min;
        this.value = Math.max(min, Math.min(max, snapped));
    }
}
