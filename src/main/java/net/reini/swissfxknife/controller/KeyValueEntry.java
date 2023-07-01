/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.reini.swissfxknife.controller;

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