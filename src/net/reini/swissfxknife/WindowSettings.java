/**
 * File Name: WindowSettings.java
 * 
 * Copyright (c) 2017 BISON Schweiz AG, All Rights Reserved.
 */

package net.reini.swissfxknife;

import java.util.prefs.Preferences;

import javafx.stage.Stage;

final class WindowSettings {
    private final Preferences prefs;

    static WindowSettings create(Preferences prefs) {
        return new WindowSettings(prefs);
    }

    private WindowSettings(Preferences prefs) {
        this.prefs = prefs;
    }

    void update(Stage primaryStage) {
        primaryStage.setX(prefs.getDouble("x", primaryStage.getX()));
        primaryStage.setY(prefs.getDouble("y", primaryStage.getY()));
        primaryStage.setWidth(prefs.getDouble("w", primaryStage.getWidth()));
        primaryStage.setHeight(prefs.getDouble("h", primaryStage.getHeight()));
    }

    void store(Stage primaryStage) {
        prefs.putDouble("x", primaryStage.getX());
        prefs.putDouble("y", primaryStage.getY());
        prefs.putDouble("w", primaryStage.getWidth());
        prefs.putDouble("h", primaryStage.getHeight());
    }
}