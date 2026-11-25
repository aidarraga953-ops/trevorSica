package com.trevor.sica.controllers;

import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.cards.*;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.forms.TrevorSearchField;
import com.trevor.sica.components.forms.TrevorTable;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.*;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

final class AdministratorModules {
    private final ShellController shell;
    private final DemoData data;

    AdministratorModules(ShellController shell) {
        this.shell = shell;
        data = shell.data;
    }

    Parent companies() {
        var screen = new ScreenLayout("administrator/companies");
        FlowPane directory = new FlowPane(18, 18);
        var search = new TrevorSearchField("Buscar por empresa o funcionario", query -> refreshCompanies(directory, query));
        screen.add("filterSlot", new TrevorCard("white", filterRow(search)));
        screen.add("directorySlot", directory);
        refreshCompanies(directory, "");
        return screen.view;
    }

    private void refreshCompanies(FlowPane directory, String query) {
        directory.getChildren().clear();
        for (Company company : data.companies) {
            if (!contains(company.name() + " " + company.officer(), query)) continue;
            StackPane mark = new StackPane(Icons.icon(company.mark(), 28));
            mark.getStyleClass().addAll("company-mark", "company-" + company.mark());
            var card = new TrevorCard("white", row(10, mark, spacer(), new TrevorStatusBadge(company.status())),
                    new VBox(3, label(company.name(), "card-title"), label(company.officer(), "muted")),
                    rule(), button("Ver empresa", "arrow", "text-button", () -> companyDetail(company, () -> refreshCompanies(directory, query))));
            card.setPrefWidth(278);
            directory.getChildren().add(card);
        }
        if (directory.getChildren().isEmpty()) directory.getChildren().add(
                new TrevorEmptyState("Sin empresas", "Prueba con Sol o Luna."));
    }

    private void companyDetail(Company company, Runnable refresh) {
        boolean active = company.status() == Status.ACTIVE;
        shell.drawer(company.name(), new VBox(14,
                new TrevorCard("soft", textBlock("Funcionario asociado", company.officer()), new TrevorStatusBadge(company.status())),
                button(active ? "Desactivar empresa" : "Activar empresa", null, "secondary-button", () ->
                        TrevorDialog.open(shell.window(), active ? "Desactivar empresa" : "Activar empresa",
                                "Actualiza el estado de " + company.name() + " en la demostración.",
                                new TrevorStatusBadge(company.status()), "Confirmar", () -> {
                                    Company updated = company.withStatus(active ? Status.INACTIVE : Status.ACTIVE);
                                    data.companies.set(data.companies.indexOf(company), updated);
                                    data.audit("Empresa actualizada", shell.profile(), company.name(), "Administración");
                                    refresh.run();
                                    companyDetail(updated, refresh);
                                    shell.notify("Empresa actualizada", company.name() + " · " + updated.status().label);
                                    return true;
                                }))));
    }

    Parent accounts() {
        var screen = new ScreenLayout("administrator/accounts");
        TrevorTable<Account> table = new TrevorTable<>();
        var search = new TrevorSearchField("Buscar por nombre o correo", ignored -> {});
        var options = new ArrayList<String>();
        options.add("Todos los perfiles");
        for (Profile profile : Profile.values()) options.add(profile.title);
        var roles = select("Filtrar por perfil", options, options.getFirst());
        Runnable refresh = () -> table.show(data.accounts.stream().filter(account ->
                contains(account.name() + " " + account.email(), search.query())
                        && (options.getFirst().equals(roles.getValue()) || account.role().title.equals(roles.getValue()))).toList());
        search.onSubmit(refresh);
        roles.setOnAction(event -> refresh.run());
        table.cells("Cuenta", 278, account -> row(9, new Portrait(account.name(), account.portrait(), 33),
                textBlock(account.name(), account.email())));
        table.text("Rol", 180, account -> account.role().title);
        table.text("Empresa", 117, account -> account.company().equals("Zona ACME") ? "—" : account.company());
        table.cells("Estado", 100, account -> new TrevorStatusBadge(account.status()));
        table.cells("Acciones", 90, account -> button("Editar", null, "text-button", () -> accountDialog(account, refresh)));
        screen.add("filterSlot", new TrevorCard("white", filterRow(search, roles,
                button("Buscar", null, "secondary-button", refresh), spacer(),
                button("Nueva cuenta", "plus", "primary-button", () -> accountDialog(null, refresh)))));
        screen.add("accountsSlot", new TrevorCard("white", heading("Directorio de cuentas", null), table));
        refresh.run();
        return screen.view;
    }

    private void accountDialog(Account existing, Runnable refresh) {
        var name = field("Nombre completo", existing == null ? "" : existing.name());
        var email = field("Correo", existing == null ? "" : existing.email());
        var role = select("Perfil", List.of(Profile.values()), existing == null ? Profile.GUARD : existing.role());
        var names = new ArrayList<String>();
        names.add("Zona ACME");
        data.companies.forEach(company -> names.add(company.name()));
        var company = select("Empresa", names, existing == null ? names.getFirst() : existing.company());
        var status = select("Estado", List.of("Activa", "Inactiva"), existing == null ? "Activa" : existing.status().label);
        var error = label("", "form-error");
        var form = new VBox(11, fieldGroup("Nombre completo", name), fieldGroup("Correo", email),
                row(12, fieldGroup("Perfil", role), fieldGroup("Empresa", company)), fieldGroup("Estado", status), error);
        String action = existing == null ? "Crear cuenta" : "Guardar cambios";
        TrevorDialog.open(shell.window(), existing == null ? "Nueva cuenta" : "Editar cuenta",
                "Selecciona el perfil y la empresa de la persona.", form, action, () -> {
                    String address = email.getText().strip();
                    if (name.getText().isBlank() || !address.matches("[^ @]+@[^ @]+\\.[^ @]+")) {
                        error.setText("Introduce un nombre y un correo válido.");
                        return false;
                    }
                    if (data.accounts.stream().anyMatch(account -> account != existing && account.email().equalsIgnoreCase(address))) {
                        error.setText("Ya existe una cuenta con ese correo.");
                        return false;
                    }
                    String companyName = role.getValue() == Profile.EMPLOYEE ? company.getValue() : "Zona ACME";
                    if (role.getValue() == Profile.EMPLOYEE && "Zona ACME".equals(companyName)) {
                        error.setText("Selecciona la empresa del funcionario.");
                        return false;
                    }
                    Account account = new Account(name.getText().strip(), address, role.getValue(), companyName,
                            "Activa".equals(status.getValue()) ? Status.ACTIVE : Status.INACTIVE, existing == null ? -1 : existing.portrait());
                    if (existing == null) data.accounts.addFirst(account);
                    else data.accounts.set(data.accounts.indexOf(existing), account);
                    data.audit(existing == null ? "Cuenta creada" : "Cuenta actualizada", shell.profile(), account.name(), "Administración");
                    refresh.run();
                    shell.notify("Cuenta guardada", account.name() + " · " + account.role().title);
                    return true;
                });
    }

    Parent roles() {
        var screen = new ScreenLayout("administrator/roles");
        var selected = select("Perfil", List.of(Profile.values()), Profile.GUARD);
        var group = new ToggleGroup();
        var all = new RadioButton("Todos los permisos");
        var enabled = new RadioButton("Solo habilitados");
        all.setToggleGroup(group);
        enabled.setToggleGroup(group);
        all.setSelected(true);
        screen.add("filterSlot", new TrevorCard("white", filterRow(fieldGroup("Perfil", selected), spacer(), all, enabled)));
        String[] categories = {"Accesos", "Supervisión", "Administración", "Empresa"};
        Route[][] routes = {
                {Route.RECORDS, Route.REQUESTS, Route.PASSES}, {Route.VISITS, Route.RESTRICTIONS, Route.INCIDENTS, Route.AUDIT},
                {Route.COMPANIES, Route.ACCOUNTS, Route.ROLES}, {Route.CALENDAR, Route.APPROVALS, Route.HISTORY}
        };
        Runnable refresh = new Runnable() {
            @Override public void run() {
                var profile = selected.getValue();
                var values = shell.permissions.computeIfAbsent(profile, ignored -> new LinkedHashMap<>());
                VBox results = screen.slot("permissionsSlot");
                results.getChildren().clear();
                for (int i = 0; i < categories.length; i++) {
                    var entries = new VBox(0);
                    for (Route route : routes[i]) {
                        boolean active = values.getOrDefault(route.name(), profile.routes.contains(route));
                        if (enabled.isSelected() && !active) continue;
                        var toggle = new ToggleButton();
                        toggle.getStyleClass().add("switch");
                        var thumb = new javafx.scene.layout.Region();
                        thumb.getStyleClass().add("switch-thumb");
                        toggle.setGraphic(thumb);
                        toggle.setAccessibleText(route.title);
                        toggle.setSelected(active);
                        toggle.setOnAction(event -> {
                            values.put(route.name(), toggle.isSelected());
                            data.audit("Permiso actualizado", shell.profile(), profile.title + " · " + route.title, "Administración");
                            shell.notify(toggle.isSelected() ? "Permiso habilitado" : "Permiso deshabilitado", route.title);
                            if (enabled.isSelected()) run();
                        });
                        var line = row(10, Icons.icon(route.icon, 17), label(route.title, "row-title"), spacer(), toggle);
                        line.getStyleClass().add("list-row");
                        entries.getChildren().add(line);
                    }
                    if (!entries.getChildren().isEmpty()) results.getChildren().add(
                            new TrevorCard("white", heading(categories[i], null), entries));
                }
                if (results.getChildren().isEmpty()) results.getChildren().add(
                        new TrevorEmptyState("Sin permisos habilitados", "Selecciona otro perfil o muestra todos los permisos."));
            }
        };
        selected.setOnAction(event -> refresh.run());
        all.setOnAction(event -> refresh.run());
        enabled.setOnAction(event -> refresh.run());
        refresh.run();
        return screen.view;
    }
}
