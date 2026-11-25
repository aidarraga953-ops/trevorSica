package com.trevor.sica.controllers;

import com.trevor.sica.app.TrevorApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;

final class ScreenLayout {
    private final FXMLLoader loader;
    final Parent view;

    ScreenLayout(String name) {
        loader = new FXMLLoader(TrevorApp.class.getResource(TrevorApp.RESOURCES + "fxml/" + name + ".fxml"));
        try {
            view = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible cargar " + name, e);
        }
    }

    VBox slot(String name) { return (VBox) loader.getNamespace().get(name); }
    void add(String name, Node... nodes) { slot(name).getChildren().addAll(nodes); }
}
