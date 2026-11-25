package com.trevor.sica.components.cards;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class TrevorCard extends VBox {
    public TrevorCard(String tone, Node... children) {
        super(12, children);
        getStyleClass().addAll("trevor-card", "surface-" + tone);
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);
    }
}
