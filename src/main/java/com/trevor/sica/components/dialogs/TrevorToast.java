package com.trevor.sica.components.dialogs;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.components.branding.Icons;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import static com.trevor.sica.components.TrevorUi.*;

public final class TrevorToast {
    private final StackPane layer;
    private HBox previous;
    private SequentialTransition dismiss;

    public TrevorToast(StackPane layer) { this.layer = layer; }

    public void show(String title, String description) {
        if (dismiss != null) dismiss.stop();
        if (previous != null) layer.getChildren().remove(previous);
        var icon = Icons.icon("check-circle", 24);
        HBox toast = row(12, icon, textBlock(title, description));
        toast.getStyleClass().add("trevor-toast");
        toast.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        toast.setMouseTransparent(true);
        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toast, new Insets(0, 25, 24, 0));
        layer.getChildren().add(toast);
        previous = toast;
        Motion.reveal(toast, 9, 0);
        Motion.confirm(icon);
        FadeTransition fade = new FadeTransition(Duration.millis(170), toast);
        fade.setToValue(0);
        dismiss = new SequentialTransition(new PauseTransition(Duration.seconds(3.0)), fade);
        dismiss.setOnFinished(event -> {
            layer.getChildren().remove(toast);
            if (previous == toast) previous = null;
        });
        dismiss.play();
    }
}
