package com.trevor.sica.app;

import com.trevor.sica.demo.DemoData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public final class TrevorApp extends Application {
    public static final String RESOURCES = "/com/trevor/sica/";

    @Override public void start(Stage stage) throws IOException {
        Locale.setDefault(Locale.Category.FORMAT, DemoData.SPANISH);
        Parent root = FXMLLoader.load(getClass().getResource(RESOURCES + "fxml/layouts/shell.fxml"));
        Scene scene = new Scene(root, 1332, 820);
        for (String theme : List.of("theme", "components", "navigation", "controls", "dashboard", "passes")) {
            scene.getStylesheets().add(getClass().getResource(RESOURCES + "css/trevor-" + theme + ".css").toExternalForm());
        }
        stage.setTitle("Trevor SICA · Zona ACME");
        stage.setMinWidth(920);
        stage.setMinHeight(605);
        stage.setScene(scene);
        stage.show();
        root.requestFocus();
    }

    public static void main(String[] args) { launch(args); }
}
