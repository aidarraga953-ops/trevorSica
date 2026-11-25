package com.trevor.sica.components.forms;

import com.trevor.sica.components.branding.Icons;
import javafx.scene.control.TextField;

import java.util.function.Consumer;

public final class TrevorSearchField extends javafx.scene.layout.HBox {
    private final TextField field = new TextField();

    public TrevorSearchField(String prompt, Consumer<String> listener) {
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        setSpacing(7);
        getStyleClass().add("search-field");
        field.setPromptText(prompt);
        field.setAccessibleText(prompt);
        var focusWithin = javafx.css.PseudoClass.getPseudoClass("focus-within");
        field.focusedProperty().addListener((obs, previous, focused) -> pseudoClassStateChanged(focusWithin, focused));
        field.textProperty().addListener((obs, previous, text) -> listener.accept(text.strip()));
        field.setMaxWidth(Double.MAX_VALUE);
        setHgrow(field, javafx.scene.layout.Priority.ALWAYS);
        getChildren().addAll(Icons.icon("search", 17), field);
        setMaxWidth(360);
    }

    public void onSubmit(Runnable action) { field.setOnAction(event -> action.run()); }
    public String query() { return field.getText().strip(); }
    public void clear() { field.clear(); }
}
