package com.trevor.sica.components.dialogs;

import com.trevor.sica.animation.Motion;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.function.BooleanSupplier;

import static com.trevor.sica.components.TrevorUi.*;

public final class TrevorDialog {
    private TrevorDialog() {}

    public static void open(Window owner, String title, String description, Node content,
                            String confirmLabel, BooleanSupplier confirm) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);
        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("trevor-dialog");
        pane.getStylesheets().addAll(owner.getScene().getStylesheets());
        pane.setPrefWidth(490);
        Label copy = label(description, "muted");
        copy.setWrapText(true);
        VBox body = new VBox(14, label(title, "dialog-title"), copy, content);
        pane.setContent(body);
        ButtonType accept = new ButtonType(confirmLabel, ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, accept);
        Button cancel = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancel.setText("Cancelar");
        cancel.getStyleClass().add("secondary-button");
        Button submit = (Button) pane.lookupButton(accept);
        submit.getStyleClass().add("primary-button");
        submit.addEventFilter(ActionEvent.ACTION, event -> {
            if (!confirm.getAsBoolean()) event.consume();
        });
        dialog.setOnShown(event -> {
            pane.getScene().setFill(Color.TRANSPARENT);
            Motion.reveal(body, 6, 0);
        });
        dialog.show();
    }
}
