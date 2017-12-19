package net.reini.swissfxknife;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class ConfigEntry {
    private final SimpleStringProperty key;
    private final SimpleStringProperty value;

    public static ConfigEntry create(String key, String value) {
        return new ConfigEntry(key, value);
    }

    private ConfigEntry(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public String getKey() {
        return key.get();
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public StringProperty valueProperty() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("ConfigEntry [key=%s, value=%s]", key, value);
    }
}