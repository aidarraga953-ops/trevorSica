package com.trevor.sica.controllers;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.cards.TrevorCard;
import com.trevor.sica.components.cards.TrevorEmptyState;
import com.trevor.sica.components.cards.TrevorStatusBadge;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.forms.TrevorSearchField;
import com.trevor.sica.components.forms.TrevorTable;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

final class SupervisorModules {
    private final ShellController shell;
    private final DemoData data;

    SupervisorModules(ShellController shell) {
        this.shell = shell;
        data = shell.data;
    }

    Parent visits() {
        var screen = new ScreenLayout("supervisor/visits");
        var table = visitTable();
        var toolbar = row(9);
        documentSearch(text -> table.show(data.visits.stream()
                .filter(visit -> text.isBlank() || visit.person().document().equals(text)).toList()), toolbar);
        var inspect = button("Ver detalle", "arrow", "secondary-button", () -> visitDetail(table.getSelectionModel().getSelectedItem()));
        inspect.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        table.setRowFactory(view -> {
            var row = new javafx.scene.control.TableRow<Visit>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) visitDetail(row.getItem());
            });
            return row;
        });
        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) visitDetail(table.getSelectionModel().getSelectedItem());
        });
        table.show(data.visits);
        screen.add("searchSlot", new TrevorCard("white", heading("Buscar por documento", null), toolbar,
                label("Documentos de ejemplo: 102030, 204060 y 306090.", "muted")));
        screen.add("visitsSlot", new TrevorCard("white", heading("Visitas", inspect), table));
        return screen.view;
    }

    private void visitDetail(Visit visit) {
        if (visit == null) return;
        Person person = visit.person();
        VBox related = new VBox(9, label("Incidentes asociados", "card-title"));
        List<Incident> matches = data.incidents.stream().filter(event ->
                event.visit().person().equals(visit.person()) && event.visit().date().equals(visit.date())
                        && event.visit().time().equals(visit.time())).toList();
        if (matches.isEmpty()) related.getChildren().add(label("Sin incidentes asociados.", "muted"));
        else matches.forEach(event -> {
            var text = label(event.description(), "row-title");
            text.setWrapText(true);
            related.getChildren().add(new TrevorCard("soft", text, label(stamp(event.date(), event.time()), "muted")));
        });
        shell.drawer("Detalle de la visita", new VBox(13,
                new TrevorCard("soft", row(12, new Portrait(person.name(), person.portrait(), 44),
                        textBlock(person.name(), "Documento " + person.document())),
                        label(person.company(), "row-title"), label(stamp(visit.date(), visit.time()), "muted"),
                        label(visit.reason(), "muted"), new TrevorStatusBadge(visit.status())),
                related, button("Registrar incidente", "plus", "primary-button", () -> newIncident(visit))));
    }

    Parent restrictions() {
        var screen = new ScreenLayout("supervisor/restrictions");
        VBox result = new VBox(12, new TrevorEmptyState("Consulta de acceso", "Prueba con 102030, 204060 o 306090."));
        var toolbar = row(9);
        documentSearch(query -> {
            Person person = data.findPerson(query);
            Node content;
            if (person == null) {
                content = new TrevorEmptyState("Sin resultados", "Verifica el número de documento.");
            } else {
                Status access = data.accessStatus(person);
                String detail = switch (access) {
                    case RESTRICTED -> "Deriva la consulta al supervisor de turno.";
                    case WAITING -> "La visita está pendiente de autorización.";
                    default -> "La persona cuenta con autorización de acceso.";
                };
                content = new TrevorCard("white",
                        row(12, new Portrait(person.name(), person.portrait(), 50),
                                textBlock(person.name(), person.company() + " · Documento " + person.document()),
                                spacer(), new TrevorStatusBadge(access)),
                        rule(), row(9, Icons.icon("shield", 17), label(detail, "muted")));
            }
            result.getChildren().setAll(content);
            Motion.reveal(content, 4, 0);
        }, toolbar);
        screen.add("searchSlot", new TrevorCard("white", heading("Estado de acceso", null), toolbar));
        screen.add("resultSlot", result);
        return screen.view;
    }

    Parent incidents() {
        var screen = new ScreenLayout("supervisor/incidents");
        var table = new TrevorTable<Incident>();
        table.cells("Visita", 192, incident -> textBlock(incident.visit().person().name(), incident.visit().person().company()));
        table.text("Fecha y hora", 166, incident -> stamp(incident.date(), incident.time()));
        table.text("Descripción", 430, Incident::description);
        table.cells("Estado", 120, incident -> new TrevorStatusBadge(Status.LOGGED));
        var from = date("Desde");
        var to = date("Hasta");
        Runnable refresh = () -> table.show(data.incidents.stream()
                .filter(i -> inRange(i.date(), from, to)).toList());
        from.setOnAction(event -> refresh.run());
        to.setOnAction(event -> refresh.run());
        screen.add("filterSlot", new TrevorCard("white", filterRow(fieldGroup("Desde", from), fieldGroup("Hasta", to),
                spacer(), button("Limpiar fechas", null, "text-button", () -> clearDates(from, to, refresh)))));
        screen.add("historySlot", new TrevorCard("white", heading("Incidentes registrados", null), table));
        refresh.run();
        return screen.view;
    }

    Parent audit() {
        var screen = new ScreenLayout("layouts/audit");
        var table = new TrevorTable<Audit>();
        table.cells("Acción", 255, event -> row(10, Icons.icon("audit", 16), textBlock(event.action(), event.type())));
        table.text("Usuario", 167, Audit::actor);
        table.text("Detalle", 285, Audit::detail);
        table.text("Fecha y hora", 164, event -> stamp(event.date(), event.time()));
        var from = date("Desde");
        var to = date("Hasta");
        var type = select("Tipo de acción", List.of("Todas", "Sesiones", "Registros", "Seguridad", "Administración", "Autorizaciones"), "Todas");
        var search = new TrevorSearchField("Buscar acción o usuario", ignored -> {});
        Runnable refresh = () -> table.show(data.audits.stream()
                .filter(event -> inRange(event.date(), from, to)
                        && ("Todas".equals(type.getValue()) || type.getValue().equals(event.type()))
                        && contains(event.actor() + " " + event.action() + " " + event.detail(), search.query()))
                .sorted(java.util.Comparator.comparing(Audit::date).thenComparing(Audit::time).reversed()).toList());
        search.onSubmit(refresh);
        from.setOnAction(event -> refresh.run());
        to.setOnAction(event -> refresh.run());
        type.setOnAction(event -> refresh.run());
        screen.add("filterSlot", new TrevorCard("white", filterRow(search,
                fieldGroup("Tipo de acción", type), fieldGroup("Desde", from), fieldGroup("Hasta", to),
                button("Aplicar", null, "primary-button", refresh))));
        screen.add("auditSlot", new TrevorCard("white", heading("Registro de actividad", null), table));
        refresh.run();
        return screen.view;
    }

    private void newIncident(Visit visit) {
        var date = date("Fecha");
        date.setValue(data.today);
        var time = field("Hora", data.currentTime().format(DemoData.TIME));
        time.setPrefColumnCount(6);
        var note = new TextArea();
        note.setWrapText(true);
        note.setPrefRowCount(3);
        var feedback = label("", "form-error");
        VBox form = new VBox(11, new TrevorCard("soft", textBlock(visit.person().name(), visit.person().company())),
                row(12, fieldGroup("Fecha", date), fieldGroup("Hora (HH:mm)", time)),
                fieldGroup("Descripción del incidente", note), feedback);
        TrevorDialog.open(shell.window(), "Registrar incidente", "Se asociará a la visita seleccionada.", form,
                "Guardar incidente", () -> {
                    if (note.getText().isBlank() || date.getValue() == null) {
                        feedback.setText("Completa la fecha y la descripción.");
                        return false;
                    }
                    try {
                        LocalTime at = LocalTime.parse(time.getText().strip(), DemoData.TIME);
                        data.incidents.addFirst(new Incident(visit, date.getValue(), at, note.getText().strip()));
                        data.audit("Incidente registrado", shell.profile(), "Visita de " + visit.person().name(), "Seguridad");
                        visitDetail(visit);
                        shell.notify("Incidente registrado", visit.person().name() + " · " + stamp(date.getValue(), at));
                        return true;
                    } catch (DateTimeParseException invalid) {
                        feedback.setText("Introduce una hora válida en formato HH:mm.");
                        return false;
                    }
                });
    }

    static DatePicker date(String caption) {
        DatePicker control = new DatePicker();
        control.setPromptText(caption);
        control.setAccessibleText(caption);
        control.setEditable(false);
        control.setPrefWidth(144);
        return control;
    }

    static String stamp(java.time.LocalDate date, LocalTime time) {
        return date.format(DemoData.DATE) + " · " + time.format(DemoData.TIME);
    }

    private static void clearDates(DatePicker from, DatePicker to, Runnable refresh) {
        from.setValue(null);
        to.setValue(null);
        refresh.run();
    }
}
