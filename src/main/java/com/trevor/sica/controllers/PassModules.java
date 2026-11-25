package com.trevor.sica.controllers;

import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.cards.TrevorCard;
import com.trevor.sica.components.cards.TrevorEmptyState;
import com.trevor.sica.components.cards.TrevorPassTicket;
import com.trevor.sica.components.cards.TrevorStatusBadge;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.forms.TrevorSearchField;
import com.trevor.sica.components.forms.TrevorSegmentedControl;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.*;
import com.trevor.sica.demo.TemporaryPass;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

final class PassModules {
    private static final int ARCHIVE = 0;
    private static final int PENDING = 1;
    private final ShellController shell;
    private final DemoData data;

    PassModules(ShellController shell) {
        this.shell = shell;
        this.data = shell.data;
    }

    Parent build() { return new PassView().layout.view; }

    private boolean canReview(TemporaryPass pass) {
        return shell.profile() == Profile.EMPLOYEE && Profile.EMPLOYEE.brief.equals(pass.person().company());
    }

    private final class PassView {
        private final ScreenLayout layout = new ScreenLayout("layouts/passes");
        private final Label approvedCount = label("", "pass-count");
        private final Label pendingCount = label("", "pass-count");
        private final TrevorSegmentedControl tabs;
        private int selected;
        private String query = "";

        PassView() {
            var icon = new StackPane(Icons.icon("ticket", 22));
            icon.getStyleClass().add("pass-banner-mark");
            var title = new VBox(3, label("Ticketera", "pass-banner-title"),
                    label("Pases de acceso con vigencia limitada.", "pass-banner-copy"));
            var banner = row(15, icon, title, spacer(), count(approvedCount, "Emitidos"), count(pendingCount, "Pendientes"));
            banner.getStyleClass().add("pass-banner");
            layout.add("summarySlot", banner);

            selected = shell.profile() == Profile.EMPLOYEE ? PENDING : ARCHIVE;
            tabs = new TrevorSegmentedControl(List.of("Comprobantes", "Pendientes", "Rechazados"), index -> {
                selected = index;
                refresh();
            });
            var search = new TrevorSearchField("Folio, nombre o documento", value -> {
                query = value;
                refresh();
            });
            search.setPrefWidth(270);
            HBox tools = row(12, tabs, spacer(), search);
            tools.getStyleClass().add("pass-tools");
            if (shell.profile() == Profile.GUARD) tools.getChildren().add(button("Expedir pase", "plus", "primary-button",
                    () -> issue(() -> {
                        search.clear();
                        tabs.select(PENDING);
                    })));
            layout.add("filterSlot", tools);
            tabs.select(selected);
        }

        private VBox count(Label value, String caption) {
            var box = new VBox(1, value, label(caption, "pass-count-label"));
            box.getStyleClass().add("pass-count-block");
            return box;
        }

        private void refresh() {
            var visible = data.passes.stream().filter(pass -> shell.profile() != Profile.EMPLOYEE || canReview(pass)).toList();
            approvedCount.setText(Long.toString(visible.stream().filter(pass -> pass.status() == Status.APPROVED).count()));
            pendingCount.setText(Long.toString(visible.stream().filter(pass -> pass.status() == Status.PENDING).count()));
            Status status = List.of(Status.APPROVED, Status.PENDING, Status.REJECTED).get(selected);
            var filtered = visible.stream().filter(pass -> pass.status() == status && contains(
                    pass.code() + " " + pass.person().name() + " " + pass.person().document(), query)).toList();
            VBox results = layout.slot("passesSlot");
            results.getChildren().clear();
            if (filtered.isEmpty()) {
                results.getChildren().add(new TrevorCard("white", new TrevorEmptyState(
                        query.isEmpty() ? emptyTitle(status) : "Sin coincidencias",
                        query.isEmpty() ? emptyCopy(status) : "Prueba con un folio, nombre o documento diferente.")));
                return;
            }

            if (selected == ARCHIVE) {
                var archive = new FlowPane(17, 20);
                archive.getStyleClass().add("pass-results");
                var now = data.now();
                for (var pass : filtered) {
                    var card = new VBox(9, new TrevorPassTicket(pass, now),
                            button("Ver comprobante", "arrow", "text-button", () -> showReceipt(pass, false)));
                    card.setPrefWidth(345);
                    archive.getChildren().add(card);
                }
                results.getChildren().add(archive);
            } else {
                for (var pass : filtered) results.getChildren().add(requestRow(pass));
            }
        }

        private TrevorCard requestRow(TemporaryPass pass) {
            Person person = pass.person();
            var dates = dateTime(pass.start()) + "  →  " + dateTime(pass.end());
            var badge = new TrevorStatusBadge(pass.status());
            var identity = row(11, new Portrait(person.name(), person.portrait(), 40),
                    textBlock(person.name(), person.company() + " · Doc. " + person.document()),
                    spacer(), label(pass.code(), "ticket-folio"), badge);
            var reason = label(pass.reason(), "row-title");
            reason.setWrapText(true);
            var text = new VBox(4, reason, label(dates, "muted"));
            var detail = row(12, text, spacer());
            if (pass.status() == Status.PENDING && canReview(pass)) {
                detail.getChildren().addAll(
                        button("Rechazar", "close", "danger-button", () -> review(pass, false)),
                        button("Aprobar y emitir", "check", "primary-button", () -> review(pass, true)));
            }
            var request = new TrevorCard("white", identity, rule(), detail);
            request.getStyleClass().add("pass-request");
            return request;
        }

        private void review(TemporaryPass pass, boolean approved) {
            String title = approved ? "Aprobar pase temporal" : "Rechazar pase temporal";
            var error = label("", "form-error");
            var body = new VBox(10, new TrevorCard("soft", textBlock(pass.person().name(), pass.reason()),
                    label("Válido hasta " + dateTime(pass.end()), "muted")), error);
            TrevorDialog.open(shell.window(), title,
                    approved ? "El comprobante se guardará en la ticketera." : "La decisión quedará en el historial de pases.",
                    body, approved ? "Aprobar y emitir" : "Confirmar rechazo", () -> {
                        var updated = data.decidePass(pass, approved, shell.profile());
                        if (updated == null) {
                            error.setText("El pase venció o ya se procesó.");
                            return false;
                        }
                        if (approved) {
                            tabs.select(ARCHIVE);
                            javafx.application.Platform.runLater(() -> showReceipt(updated, true));
                        } else {
                            tabs.select(2);
                            shell.notify("Pase rechazado", pass.code() + " · " + pass.person().name());
                        }
                        return true;
                    });
        }
    }

    private void showReceipt(TemporaryPass pass, boolean animated) {
        var ticket = new TrevorPassTicket(pass, data.now());
        var title = label(animated ? "Pase aprobado" : "Comprobante de acceso", "card-title");
        var wrapper = new VBox(17, new VBox(7, title,
                label(pass.code(), "muted")), ticket,
                new TrevorCard("soft", textBlock("Motivo", pass.reason()), textBlock("Expedido por", pass.issuer())));
        wrapper.getStyleClass().add("pass-receipt");
        shell.drawer("Pase temporal", wrapper);
        if (animated) ticket.animateTear(() -> title.setText("Guardado en la ticketera"));
    }

    private void issue(Runnable issued) {
        var name = field("Nombre completo", "");
        var document = field("Documento", "");
        var companies = data.companies.stream().filter(company -> company.status() == Status.ACTIVE
                && company.name().equals(Profile.EMPLOYEE.brief)).map(Company::name).toList();
        var company = select("Empresa anfitriona", companies, companies.isEmpty() ? null : companies.getFirst());
        var now = data.now().withSecond(0).withNano(0);
        var day = SupervisorModules.date("Fecha de inicio");
        day.setValue(now.toLocalDate());
        var time = field("Hora de inicio", now.toLocalTime().format(DemoData.TIME));
        time.setPrefColumnCount(6);
        var durations = List.of("1 hora", "2 horas", "4 horas", "8 horas");
        var duration = select("Duración", durations, durations.get(2));
        var reason = new TextArea();
        reason.setAccessibleText("Motivo del pase");
        reason.setPrefRowCount(2);
        reason.setWrapText(true);
        var error = label("", "form-error");
        error.setWrapText(true);
        var form = new VBox(10, fieldGroup("Nombre completo", name), fieldGroup("Documento", document),
                fieldGroup("Empresa anfitriona", company),
                row(10, fieldGroup("Fecha de inicio", day), fieldGroup("Hora (HH:mm)", time), fieldGroup("Duración", duration)),
                fieldGroup("Motivo del pase", reason), error);
        TrevorDialog.open(shell.window(), "Expedir pase temporal", "La empresa anfitriona revisará el pase antes de activarlo.",
                form, "Enviar a aprobación", () -> {
                    if (name.getText().isBlank() || document.getText().isBlank() || company.getValue() == null
                            || day.getValue() == null || reason.getText().isBlank()) {
                        error.setText(companies.isEmpty() ? "Activa Empresa Sol para expedir un pase en la demo."
                                : "Completa todos los campos.");
                        return false;
                    }
                    try {
                        var start = LocalDateTime.of(day.getValue(), LocalTime.parse(time.getText().strip(), DemoData.TIME));
                        var length = List.of(1, 2, 4, 8).get(duration.getSelectionModel().getSelectedIndex());
                        var end = start.plusHours(length);
                        if (!end.isAfter(data.now())) {
                            error.setText("La vigencia del pase debe terminar en el futuro.");
                            return false;
                        }
                        Person person = data.findPerson(document.getText());
                        if (person != null && person.access() == Status.RESTRICTED) {
                            error.setText("Este documento tiene una restricción de acceso.");
                            return false;
                        }
                        if (person != null && (!person.name().equalsIgnoreCase(name.getText().strip())
                                || !person.company().equals(company.getValue()))) {
                            error.setText("El documento está asociado a " + person.name() + " de " + person.company() + ".");
                            return false;
                        }
                        if (person == null) person = new Person(name.getText().strip(), document.getText().strip(),
                                company.getValue(), -1, Status.WAITING);
                        var pass = data.requestPass(person, reason.getText().strip(), start, end, shell.profile());
                        issued.run();
                        shell.notify("Pase enviado a aprobación", pass.code() + " · " + person.name());
                        return true;
                    } catch (DateTimeParseException invalid) {
                        error.setText("Introduce una hora válida en formato HH:mm.");
                        return false;
                    }
                });
    }

    private static String dateTime(LocalDateTime value) { return SupervisorModules.stamp(value.toLocalDate(), value.toLocalTime()); }

    private static String emptyTitle(Status status) {
        return switch (status) {
            case APPROVED -> "La ticketera está vacía";
            case PENDING -> "Sin pases pendientes";
            default -> "Sin pases rechazados";
        };
    }

    private static String emptyCopy(Status status) {
        return switch (status) {
            case APPROVED -> "Los comprobantes aparecen aquí cuando la empresa aprueba el pase.";
            case PENDING -> "Los pases que esperan aprobación aparecerán aquí.";
            default -> "Aquí quedan disponibles los pases que la empresa rechazó.";
        };
    }
}
