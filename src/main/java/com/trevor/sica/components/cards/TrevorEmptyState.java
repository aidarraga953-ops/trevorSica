package com.trevor.sica.components.cards;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.components.branding.TrevorLogo;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static com.trevor.sica.components.TrevorUi.label;

public final class TrevorEmptyState extends VBox {
    public TrevorEmptyState(String title, String detail) {
        super(8);
        getStyleClass().add("empty-state");
        setAlignment(Pos.CENTER);
        var mark = TrevorLogo.mark(43, Color.web("#94AE8B"));
        Label copy = label(detail, "muted");
        copy.setWrapText(true);
        getChildren().addAll(mark, label(title, "row-title"), copy);
        Motion.confirm(mark);
    }
}
