package com.swrobotics.lib.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder to construct dropdown selectors similar to SendableChooser for use in ShuffleLog. The
 * value is stored as a String in NetworkTables.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * NTMultiSelect<String> selectorTest = new NTSelectorBuilder<String>("Test/Selector Test")
 *             .addDefaultOption("Name 1 (def)", "Value 1")
 *             .addOption("Name 2", "Value 2")
 *             .addOption("Name 3", "Value 3")
 *             .build();
 * }</pre>
 *
 * @param <T> type of element to select
 */
public final class NTSelectorBuilder<T> {
    private static final class Option<T> {
        private final String name;
        private final T value;

        public Option(String name, T value) {
            this.name = name;
            this.value = value;
        }
    }

    private final String path;
    private final List<Option<T>> options;
    private Option<T> defaultOption;

    public NTSelectorBuilder(String path) {
        this.path = path;
        options = new ArrayList<>();
    }

    public NTSelectorBuilder<T> addOption(String name, T value) {
        options.add(new Option<>(name, value));
        return this;
    }

    public NTSelectorBuilder<T> addDefaultOption(String name, T value) {
        Option<T> option = new Option<>(name, value);
        options.add(option);
        defaultOption = option;
        return this;
    }

    public NTMultiSelect<T> build() {
        if (defaultOption == null) throw new IllegalStateException("Default option not set");

        Map<String, T> nameToVal = new HashMap<>();
        Map<T, String> valToName = new HashMap<>();
        for (Option<T> option : options) {
            nameToVal.put(option.name, option.value);
            valToName.put(option.value, option.name);
        }

        NTMultiSelect<T> selector =
                new NTMultiSelect<>(path, defaultOption.value) {
                    @Override
                    protected T valueOf(String name) {
                        if (!nameToVal.containsKey(name)) throw new IllegalArgumentException();
                        return nameToVal.get(name);
                    }

                    @Override
                    protected String nameOf(T t) {
                        return valToName.get(t);
                    }
                };

        List<T> values = new ArrayList<>();
        for (Option<T> option : options) values.add(option.value);

        selector.setOptions(values);

        return selector;
    }
}
