package com.trevor.sica.components.navigation;

import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.demo.DemoData.Profile;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import java.util.function.Consumer;

import static com.trevor.sica.components.TrevorUi.*;

public final class TrevorHeader extends HBox {
    private final Button account;
    private final ComboBox<Profile> selector = new ComboBox<>(FXCollections.observableArrayList(Profile.values()));
    private boolean synchronizing;

    public TrevorHeader(Consumer<Profile> onSelect, Runnable onNotifications, Runnable onAccount) {
        super(14);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        getStyleClass().add("trevor-header");

        HBox identity = row(10, label("trevor.", "wordmark"), label("SICA", "brand-sica"));
        Region divider = new Region();
        divider.getStyleClass().add("header-divider");
        Label site = label("Zona ACME", "site-name");
        selector.setValue(Profile.GUARD);
        selector.setAccessibleText("Selector de perfil de demostración");
        selector.setOnAction(event -> {
            if (!synchronizing) onSelect.accept(selector.getValue());
        });
        HBox demo = row(9, label("Modo demo", "demo-label"), selector);
        demo.getStyleClass().add("demo-selector");
        Button notifications = iconButton("Notificaciones", "bell", onNotifications);
        notifications.getStyleClass().add("header-icon");
        account = button("", null, "header-account", onAccount);
        account.setAccessibleText("Información del usuario");
        tooltip(account, "Información del usuario");
        getChildren().addAll(identity, divider, site, spacer(), demo, notifications, account);
    }

    public void setProfile(Profile profile) {
        synchronizing = true;
        selector.setValue(profile);
        synchronizing = false;
        account.setGraphic(new Portrait(profile.name, profile.portrait, 30));
    }
}
