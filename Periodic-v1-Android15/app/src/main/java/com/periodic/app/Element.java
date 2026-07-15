package com.periodic.app;

public final class Element {
    public final int number, period, group;
    public final String symbol, name, mass, category;

    public Element(int number, String symbol, String name, String mass, int period, int group, String category) {
        this.number = number;
        this.symbol = symbol;
        this.name = name;
        this.mass = mass;
        this.period = period;
        this.group = group;
        this.category = category;
    }

    public int neutrons() {
        try { return Math.max(0, Math.round(Float.parseFloat(mass.replace("[", "").replace("]", ""))) - number); }
        catch (Exception ignored) { return number; }
    }
}
