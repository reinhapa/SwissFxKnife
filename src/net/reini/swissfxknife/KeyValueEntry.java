package net.reini.swissfxknife;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class KeyValueEntry {
    private final SimpleStringProperty key;
    private final SimpleStringProperty value;

    public static KeyValueEntry create(String key, String value) {
        return new KeyValueEntry(key, value);
    }

    private KeyValueEntry(String key, String value) {
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
        return String.format("KeyValueEntry [key=%s, value=%s]", key, value);
    }
}