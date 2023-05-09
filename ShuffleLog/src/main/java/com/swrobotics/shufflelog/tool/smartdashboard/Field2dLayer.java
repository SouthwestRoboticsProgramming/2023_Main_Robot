package com.swrobotics.shufflelog.tool.smartdashboard;

import com.swrobotics.shufflelog.tool.field.FieldLayer;
import imgui.ImGui;
import imgui.type.ImBoolean;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Field2dLayer implements FieldLayer {
    private final SmartDashboard smartDashboard;
    private final List<Field2d> fields;
    private final Map<String, ImBoolean> showFields;

    public Field2dLayer(SmartDashboard smartDashboard) {
        this.smartDashboard = smartDashboard;
        fields = new ArrayList<>();
        showFields = new HashMap<>();
    }

    @Override
    public String getName() {
        return "SmartDashboard Field2d";
    }

    private ImBoolean getShow(Field2d field) {
        return showFields.computeIfAbsent(field.getName(), (k) -> new ImBoolean(false));
    }

    @Override
    public void draw(PGraphics g) {
        fields.clear();
        fields.addAll(smartDashboard.getFields());
        fields.sort((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

        for (Field2d field : fields) {
            ImBoolean show = getShow(field);
            if (show.get())
                field.draw(g);
        }
    }

    @Override
    public void showGui() {
        for (Field2d field : fields) {
            ImBoolean show = getShow(field);
            ImGui.checkbox(field.getName(), show);
        }
    }
}
