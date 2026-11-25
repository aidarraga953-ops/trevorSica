package com.trevor.sica.components.cards;

import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.demo.DemoData.Profile;

import static com.trevor.sica.components.TrevorUi.*;

public final class TrevorProfileCard extends TrevorCard {
    public TrevorProfileCard(String title, Profile profile) {
        super("white");
        getStyleClass().add("compact-profile");
        getChildren().addAll(label(title, "card-kicker"),
                row(12, new Portrait(profile.name, profile.portrait, 42), textBlock(profile.name, profile.title)));
    }
}
