package com.trevor.sica.controllers;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.app.TrevorApp;
import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.TrevorLogo;
import com.trevor.sica.demo.DemoData.Profile;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.function.Consumer;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

public final class LoginController {
    private static final String DEMO_PASSWORD = "TrevorDemo21";
    private static final PseudoClass COMPACT = PseudoClass.getPseudoClass("compact");
    @FXML private StackPane root;
    @FXML private ImageView forest;
    @FXML private HBox masthead;
    @FXML private HBox footer;
    @FXML private VBox editorial;
    @FXML private StackPane glass;
    @FXML private VBox form;

    private Consumer<Profile> onEnter;
    private javafx.scene.control.ComboBox<Profile> profiles;
    private TextField email;
    private PasswordField password;
    private ToggleButton eye;
    private Label error;

    @FXML private void initialize() {
        setBackdrop();
        var identity = row(11, new TrevorLogo(44), label("trevor.", "login-wordmark"), label("SICA", "login-sica"));
        masthead.getChildren().setAll(identity, spacer(), label("ZONA ACME", "login-site"));
        var lead = label("Cada acceso,", "login-headline");
        var emphasis = label("en buenas manos.", "login-headline-accent");
        lead.setWrapText(true);
        emphasis.setWrapText(true);
        editorial.getChildren().setAll(label("CONTROL DE ACCESOS", "login-caption"), new VBox(0, lead, emphasis));
        footer.getChildren().setAll(label("Trevor SICA  ·  Sistema de control de accesos", "login-footer"));
        buildForm();
    }

    public void setOnEnter(Consumer<Profile> action) { onEnter = action; }

    public void show(Profile profile) {
        eye.setSelected(false);
        profiles.setValue(profile);
        fillDemoFields();
        form.requestFocus();
        Motion.reveal(form, 9, 0);
    }

    private void buildForm() {
        profiles = select("Perfil de demostración", List.of(Profile.values()), Profile.GUARD);
        profiles.setMaxWidth(Double.MAX_VALUE);
        email = field("Correo", "");
        email.setPromptText("nombre@ejemplo.com");
        password = new PasswordField();
        password.setAccessibleText("Contraseña");
        password.setPromptText("Contraseña de demostración");
        var shown = field("Contraseña visible", "");
        shown.setPromptText(password.getPromptText());
        shown.textProperty().bindBidirectional(password.textProperty());
        var reveal = new SimpleBooleanProperty();
        shown.visibleProperty().bind(reveal);
        shown.managedProperty().bind(reveal);
        password.visibleProperty().bind(reveal.not());
        password.managedProperty().bind(reveal.not());
        eye = new ToggleButton("", Icons.icon("eye", 18));
        eye.getStyleClass().add("login-reveal");
        eye.setAccessibleText("Mostrar contraseña");
        reveal.bind(eye.selectedProperty());
        eye.selectedProperty().addListener((obs, wasVisible, visible) ->
                eye.setAccessibleText(visible ? "Ocultar contraseña" : "Mostrar contraseña"));
        var passwordFields = new StackPane(password, shown);
        HBox.setHgrow(passwordFields, Priority.ALWAYS);
        var passwordRow = row(6, passwordFields, eye);
        passwordRow.getStyleClass().add("login-password");
        var focused = new SimpleBooleanProperty();
        focused.bind(password.focusedProperty().or(shown.focusedProperty()));
        var focusWithin = PseudoClass.getPseudoClass("focus-within");
        focused.addListener((obs, before, active) -> passwordRow.pseudoClassStateChanged(focusWithin, active));
        error = label("", "login-error");
        error.setWrapText(true);
        error.managedProperty().bind(error.textProperty().isNotEmpty());
        error.visibleProperty().bind(error.managedProperty());
        var reset = button("Restaurar datos de demo", null, "login-reset", this::fillDemoFields);
        var help = new VBox(4, error, reset);
        help.managedProperty().bind(error.managedProperty());
        help.visibleProperty().bind(error.managedProperty());
        profiles.setOnAction(event -> fillDemoFields());
        fillDemoFields();
        var enter = button("Entrar a Trevor", "arrow", "login-submit", this::submit);
        enter.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        enter.setMaxWidth(Double.MAX_VALUE);
        email.setOnAction(event -> submit());
        password.setOnAction(event -> submit());
        shown.setOnAction(event -> submit());
        var heading = new VBox(4, label("Iniciar sesión", "login-title"), label("Elige un perfil para explorar la demo.", "login-copy"));
        form.getChildren().setAll(heading, fieldGroup("Perfil de demostración", profiles), fieldGroup("Correo", email),
                new VBox(6, label("Contraseña", "field-label"), passwordRow), help, enter);
    }

    private void fillDemoFields() {
        if (profiles.getValue() != null) {
            email.setText(profiles.getValue().demoEmail());
            password.setText(DEMO_PASSWORD);
            error.setText("");
        }
    }

    private void submit() {
        Profile profile = profiles.getValue();
        if (profile == null || !profile.demoEmail().equalsIgnoreCase(email.getText().strip())
                || !DEMO_PASSWORD.equals(password.getText())) {
            error.setText("Vuelve a elegir un perfil para rellenar sus datos de demostración.");
            return;
        }
        if (onEnter != null) onEnter.accept(profile);
    }

    private void setBackdrop() {
        Image background = new Image(TrevorApp.class.getResource(TrevorApp.RESOURCES + "images/trevor-forest.png").toExternalForm());
        forest.setImage(background);
        forest.fitWidthProperty().bind(root.widthProperty());
        forest.fitHeightProperty().bind(root.heightProperty());

        ImageView frosted = new ImageView(background);
        frosted.viewportProperty().bind(forest.viewportProperty());
        frosted.fitWidthProperty().bind(forest.fitWidthProperty());
        frosted.fitHeightProperty().bind(forest.fitHeightProperty());
        frosted.setEffect(new BoxBlur(20, 20, 2));
        frosted.setManaged(false);
        var tint = new Region();
        tint.getStyleClass().add("login-glass-tint");
        tint.prefWidthProperty().bind(glass.widthProperty());
        tint.prefHeightProperty().bind(glass.heightProperty());
        StackPane inset = new StackPane(frosted, tint);
        inset.setManaged(false);
        inset.setMouseTransparent(true);
        Rectangle corners = new Rectangle();
        corners.widthProperty().bind(glass.widthProperty());
        corners.heightProperty().bind(glass.heightProperty());
        corners.setArcWidth(58);
        corners.setArcHeight(58);
        inset.setClip(corners);
        glass.getChildren().addFirst(inset);
        glass.layoutBoundsProperty().addListener((obs, old, bounds) ->
                inset.resize(bounds.getWidth(), bounds.getHeight()));
        glass.localToSceneTransformProperty().addListener((obs, old, transform) -> {
            frosted.setLayoutX(-transform.getTx());
            frosted.setLayoutY(-transform.getTy());
        });
        Runnable resize = () -> {
            double width = root.getWidth();
            double height = root.getHeight();
            if (width <= 0 || height <= 0) return;
            double scale = Math.max(width / background.getWidth(), height / background.getHeight());
            double cropWidth = width / scale;
            double cropHeight = height / scale;
            forest.setViewport(new Rectangle2D((background.getWidth() - cropWidth) / 2,
                    (background.getHeight() - cropHeight) / 2, cropWidth, cropHeight));
            root.pseudoClassStateChanged(COMPACT, width < 1100 || height < 660);
        };
        root.widthProperty().addListener((obs, old, width) -> resize.run());
        root.heightProperty().addListener((obs, old, height) -> resize.run());
    }
}
