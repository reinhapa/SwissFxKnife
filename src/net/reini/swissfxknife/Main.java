package net.reini.swissfxknife;

import java.io.IOException;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(getClass());
            WindowSettings settings = WindowSettings.create(prefs);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
            Scene scene = new Scene(loader.load());
            Controller controller = loader.<Controller>getController();
            controller.initialize(primaryStage, prefs);
            primaryStage.setTitle("Reini's SwissFxKnife");
            primaryStage.setScene(scene);
            settings.update(primaryStage);
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> settings.store(primaryStage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
