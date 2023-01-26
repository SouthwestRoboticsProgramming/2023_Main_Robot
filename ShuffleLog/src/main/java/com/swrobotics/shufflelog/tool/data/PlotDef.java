package com.swrobotics.shufflelog.tool.data;

public final class PlotDef {
    private final String name;
    private final String path;
    private final ValueAccessor<?> acc;

    public PlotDef(String name, String path, ValueAccessor<?> acc) {
        this.name = name;
        this.path = path;
        this.acc = acc;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public ValueAccessor<?> getAcc() {
        return acc;
    }
}
