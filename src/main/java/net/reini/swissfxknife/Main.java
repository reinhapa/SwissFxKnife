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
