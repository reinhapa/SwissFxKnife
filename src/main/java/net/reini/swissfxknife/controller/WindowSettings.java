/**
 * File Name: WindowSettings.java
 * 
 * Copyright (c) 2017 BISON Schweiz AG, All Rights Reserved.
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