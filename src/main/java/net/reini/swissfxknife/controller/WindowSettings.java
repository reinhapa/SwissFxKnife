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

import java.util.prefs.Preferences;

import javafx.stage.Stage;

public final class WindowSettings {
    private final Preferences prefs;

    public static WindowSettings create(Preferences prefs) {
        return new WindowSettings(prefs);
    }

    private WindowSettings(Preferences prefs) {
        this.prefs = prefs;
    }

    public void reset() {
        prefs.remove("x");
        prefs.remove("y");
        prefs.remove("w");
        prefs.remove("h");
    }

    public void update(Stage primaryStage) {
        primaryStage.setX(prefs.getDouble("x", primaryStage.getX()));
        primaryStage.setY(prefs.getDouble("y", primaryStage.getY()));
        primaryStage.setWidth(prefs.getDouble("w", primaryStage.getWidth()));
        primaryStage.setHeight(prefs.getDouble("h", primaryStage.getHeight()));
    }

    public void store(Stage primaryStage) {
        prefs.putDouble("x", primaryStage.getX());
        prefs.putDouble("y", primaryStage.getY());
        prefs.putDouble("w", primaryStage.getWidth());
        prefs.putDouble("h", primaryStage.getHeight());
    }
}