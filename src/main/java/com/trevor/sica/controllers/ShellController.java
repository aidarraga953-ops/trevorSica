package com.trevor.sica.controllers;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.cards.TrevorCard;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.dialogs.TrevorDrawer;
import com.trevor.sica.components.dialogs.TrevorToast;
import com.trevor.sica.components.navigation.TrevorHeader;
import com.trevor.sica.components.navigation.TrevorSidebar;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.Profile;
import com.trevor.sica.demo.DemoData.Route;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

import static com.trevor.sica.components.TrevorUi.*;

public final class ShellController {
    private static final DateTimeFormatter LONG_DATE =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", DemoData.SPANISH);
    @FXML private StackPane root;
    @FXML private StackPane sidebarSlot;
    @FXML private StackPane headerSlot;
    @FXML private ScrollPane scroll;
    @FXML private VBox page;

    private Profile profile = Profile.GUARD;
    private Route route = Route.HOME;
    private TrevorSidebar sidebar;
    private TrevorHeader header;
    private TrevorDrawer drawer;
    private TrevorToast toast;
    private ModuleController modules;
    private DashboardController dashboards;

    final DemoData data = new DemoData();
    final Map<Profile, Map<String, Boolean>> permissions = new EnumMap<>(Profile.class);
    private boolean notificationsEnabled = true;

    @FXML private void initialize() {
        drawer = new TrevorDrawer(root);
        toast = new TrevorToast(root);
        sidebar = new TrevorSidebar(this::navigate, this::settings, this::signOut, this::account);
        header = new TrevorHeader(this::selectProfile, this::notifications, this::account);
        modules = new ModuleController(this);
        dashboards = new DashboardController(this);
        sidebarSlot.getChildren().add(sidebar);
        headerSlot.getChildren().add(header);
        sidebar.prefHeightProperty().bind(sidebarSlot.heightProperty());
        root.widthProperty().addListener((obs, previous, now) -> {
            boolean compact = now.doubleValue() < 1080;
            sidebar.setCompactMode(compact);
            root.pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("compact"), compact);
        });
        selectProfile(Profile.GUARD);
    }

    Profile profile() { return profile; }
    Window window() { return root.getScene().getWindow(); }
    void notify(String title, String message) { toast.show(title, message); }
    void drawer(String title, Node content) { drawer.open(title, content); }
    void newRequest(Runnable onCreated) { modules.newRequest(onCreated); }

    private void selectProfile(Profile selected) {
        if (selected == null) return;
        profile = selected;
        sidebar.setProfile(selected);
        header.setProfile(selected);
        navigate(Route.HOME);
    }

    void navigate(Route target) {
        if (!profile.routes.contains(target)) return;
        route = target;
        drawer.close();
        sidebar.select(target);
        render();
        scroll.setVvalue(0);
    }

    private void render() {
        boolean home = route == Route.HOME;
        String title = home ? switch (profile) {
            case GUARD -> "Hola, Carlos.";
            case SUPERVISOR -> "Hola, Andrés.";
            case ADMIN -> "Hola, Administrador.";
            case EMPLOYEE -> "Hola, Valentina.";
        } : route.title;
        String description = home ? switch (profile) {
            case GUARD -> "Registra accesos y consulta las próximas llegadas.";
            case SUPERVISOR -> "Da seguimiento a accesos, visitas e incidentes.";
            case ADMIN -> "Gestiona las empresas y cuentas de Zona ACME.";
            case EMPLOYEE -> "Organiza las visitas y autorizaciones de Empresa Sol.";
        } : route.description;
        var intro = new VBox(4, label(title, "page-title"), label(description, "page-description"));
        var date = row(8, Icons.icon("calendar", 16), label(capitalize(data.today.format(LONG_DATE)), "date-text"));
        date.getStyleClass().add("date-pill");
        date.setMaxHeight(Region.USE_PREF_SIZE);
        HBox pageHeader = row(12, intro, spacer(), date);
        pageHeader.getStyleClass().add("page-header");
        if (!home) pageHeader.getStyleClass().add("module-header");
        Node screen = home ? dashboards.build(profile) : modules.build(route);
        page.getChildren().setAll(pageHeader, screen);
        Motion.reveal(pageHeader, 3, 0);
        Motion.reveal(screen, 4, 30);
    }

    private static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase(DemoData.SPANISH) + value.substring(1);
    }

    private void settings() {
        CheckBox enabled = new CheckBox("Mostrar avisos de actividad");
        enabled.setSelected(notificationsEnabled);
        drawer("Configuración", new VBox(14, label("Notificaciones de demostración", "row-title"), enabled,
                button("Guardar preferencias", "check", "primary-button", () -> {
                    notificationsEnabled = enabled.isSelected();
                    drawer.close();
                    notify("Preferencias guardadas", "Los cambios se aplicaron a esta sesión.");
                })));
    }

    private void signOut() {
        TrevorDialog.open(window(), "Cerrar sesión de demostración",
                "Volverás al inicio del perfil actual.",
                new Region(), "Cerrar sesión", () -> {
                    data.audit("Cierre de sesión", profile, "Sesión de demostración", "Sesiones");
                    navigate(Route.HOME);
                    notify("Sesión finalizada", "El selector de perfiles sigue disponible.");
                    return true;
                });
    }

    private void account() {
        drawer("Información del usuario", new TrevorCard("soft",
                row(12, new Portrait(profile.name, profile.portrait, 52), textBlock(profile.name, profile.title)),
                rule(), label(profile.brief, "muted"), label("Zona ACME", "row-title")));
    }

    private void notifications() {
        VBox events = new VBox(11);
        if (notificationsEnabled) {
            data.audits.stream().limit(4).forEach(event -> events.getChildren().add(
                    new TrevorCard("soft", textBlock(event.action(), event.detail()), label(event.time().format(DemoData.TIME), "muted"))));
        } else {
            events.getChildren().add(label("Los avisos están desactivados.", "muted"));
        }
        drawer("Notificaciones", events);
    }
}
