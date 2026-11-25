package com.trevor.sica.components.cards;

import com.trevor.sica.demo.DemoData.Status;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public final class TrevorStatusBadge extends Label {
    public TrevorStatusBadge(Status status) {
        super(status.label);
        Region dot = new Region();
        dot.getStyleClass().add("badge-dot");
        setGraphic(dot);
        setGraphicTextGap(6);
        getStyleClass().addAll("status-badge", "status-" + status.tone);
    }
}
