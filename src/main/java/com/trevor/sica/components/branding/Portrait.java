package com.trevor.sica.components.branding;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Portrait extends StackPane {
    private static final Image SHEET = new Image(Portrait.class
            .getResource("/com/trevor/sica/images/portraits.png").toExternalForm());

    public Portrait(String name, int image, double size) {
        getStyleClass().add("portrait");
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        if (image >= 0) {
            double w = SHEET.getWidth() / 2;
            double h = SHEET.getHeight() / 2;
            ImageView view = new ImageView(SHEET);
            view.setViewport(new Rectangle2D((image % 2) * w + w * .09, (image / 2) * h + h * .07,
                    w * .82, h * .82));
            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true);
            view.setClip(new Circle(size / 2, size / 2, size / 2));
            getChildren().add(view);
        } else {
            String initials = Arrays.stream(name.trim().split("\\s+")).filter(s -> !s.isEmpty())
                    .limit(2).map(s -> s.substring(0, 1).toUpperCase()).collect(Collectors.joining());
            Label label = new Label(initials);
            label.getStyleClass().add("initials");
            label.setStyle("-fx-font-size: " + Math.round(size * .32) + "px;");
            getStyleClass().add("portrait-tone-" + Math.floorMod(name.hashCode(), 3));
            getChildren().add(label);
        }
        setAccessibleText(name);
    }
}
