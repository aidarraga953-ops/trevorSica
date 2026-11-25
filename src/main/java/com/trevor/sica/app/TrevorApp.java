package com.trevor.sica.app;

import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.Profile;
import com.trevor.sica.controllers.LoginController;
import com.trevor.sica.controllers.ShellController;
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
        var loginLoader = new FXMLLoader(getClass().getResource(RESOURCES + "fxml/layouts/login.fxml"));
        Parent loginView = loginLoader.load();
        LoginController login = loginLoader.getController();
        var shellLoader = new FXMLLoader(getClass().getResource(RESOURCES + "fxml/layouts/shell.fxml"));
        Parent shellView = shellLoader.load();
        ShellController shell = shellLoader.getController();
        Scene scene = new Scene(loginView, 1332, 820);
        for (String theme : List.of("theme", "components", "navigation", "controls", "dashboard", "login", "passes")) {
            scene.getStylesheets().add(getClass().getResource(RESOURCES + "css/trevor-" + theme + ".css").toExternalForm());
        }
        login.setOnEnter(profile -> {
            scene.setRoot(shellView);
            shell.enter(profile);
            shellView.requestFocus();
        });
        shell.setOnSignOut(profile -> {
            scene.setRoot(loginView);
            login.show(profile);
        });
        stage.setTitle("Trevor SICA · Zona ACME");
        stage.setMinWidth(920);
        stage.setMinHeight(605);
        stage.setScene(scene);
        stage.show();
        login.show(Profile.GUARD);
    }

    public static void main(String[] args) { launch(args); }
}
