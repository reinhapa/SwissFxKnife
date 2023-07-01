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
package net.reini.swissfxknife;

import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.reini.swissfxknife.controller.Controller;
import net.reini.swissfxknife.controller.WindowSettings;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        WindowSettings settings = WindowSettings.create(prefs);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(loader.load());
        Controller controller = loader.getController();
        controller.initialize(primaryStage, prefs);
        primaryStage.setTitle("Reini's SwissFxKnife");
        primaryStage.setScene(scene);
        settings.update(primaryStage);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> settings.store(primaryStage));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
