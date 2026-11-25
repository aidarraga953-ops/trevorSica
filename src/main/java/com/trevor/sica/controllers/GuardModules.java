package com.trevor.sica.controllers;

import com.trevor.sica.animation.Motion;
import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.cards.*;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.forms.TrevorSegmentedControl;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.*;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

final class GuardModules {
    private final ShellController shell;
    private final DemoData data;

    GuardModules(ShellController shell) {
        this.shell = shell;
        this.data = shell.data;
    }

    Parent records() {
        var screen = new ScreenLayout("guard/records");
        VBox result = new VBox();
        show(result, new TrevorEmptyState("Consulta un documento", "Prueba con 102030, 204060 o 306090."));
        VBox movements = new VBox();
        Runnable refresh = () -> movements.getChildren().setAll(data.movements.stream()
                .limit(9).map(DashboardController::movementRow).toList());
        refresh.run();
        var category = new SimpleIntegerProperty(0);
        var searchRow = row(9);
        final String[] lastQuery = {""};
        Consumer<String> search = query -> {
            lastQuery[0] = query;
            Person person = data.findPerson(query);
            if (person == null) {
                show(result, new TrevorEmptyState(query.isBlank() ? "Introduce un documento" : "Sin resultados",
                        "Prueba con 102030, 204060 o 306090."));
                return;
            }
            Status mode = List.of(Status.ENTRY, Status.WORK, Status.EXIT).get(category.get());
            Status access = data.accessStatus(person);
            var identity = row(12, new Portrait(person.name(), person.portrait(), 54),
                    textBlock(person.name(), person.company() + " · Documento " + person.document()), spacer(),
                    new TrevorStatusBadge(access));
            String accessTime = data.visits.stream()
                    .filter(visit -> visit.person().equals(person) && visit.date().equals(data.today)
                            && visit.status() != Status.REJECTED)
                    .findFirst().map(visit -> visit.time().format(DemoData.TIME)).orElse("Sin visita programada");
            var record = button("Confirmar " + mode.label.toLowerCase(DemoData.SPANISH), "check", "primary-button", () -> {
                Movement movement = new Movement(person, data.today, data.currentTime(), mode);
                data.movements.addFirst(movement);
                data.audit("Registro de " + mode.label.toLowerCase(DemoData.SPANISH), shell.profile(),
                        person.name() + " · " + person.company(), "Registros");
                refresh.run();
                var indicator = Icons.icon("check-circle", 29);
                show(result, new TrevorCard("soft", row(11, indicator, textBlock("Registro exitoso",
                        person.name() + " · " + mode.label + " · " + movement.time().format(DemoData.TIME)))));
                Motion.confirm(indicator);
                shell.notify("Registro guardado", mode.label + " de " + person.name());
            });
            record.setDisable(access != Status.AUTHORIZED);
            show(result, new TrevorCard("soft", identity, rule(),
                    row(8, Icons.icon("clock", 17), label(accessTime, "muted"), spacer(), record)));
        };
        documentSearch(search, searchRow);
        var segments = new TrevorSegmentedControl(List.of("Entrada", "Ingreso laboral", "Salida"), index -> {
            category.set(index);
            if (!lastQuery[0].isBlank()) search.accept(lastQuery[0]);
        });
        screen.add("searchSlot", new TrevorCard("white", heading("Identificación", Icons.icon("id", 19)),
                segments, searchRow, result));
        screen.add("movementsSlot", new TrevorCard("white", heading("Movimientos del día", null), movements));
        return screen.view;
    }

    private void show(VBox container, javafx.scene.Node node) {
        container.getChildren().setAll(node);
        Motion.reveal(node, 4, 0);
    }

    Parent requests() {
        var screen = new ScreenLayout("guard/requests");
        var table = visitTable();
        var selected = new SimpleIntegerProperty(0);
        Runnable refresh = () -> table.show(data.visits.stream()
                .filter(v -> switch (selected.get()) {
                    case 1 -> v.status() == Status.PENDING;
                    case 2 -> v.status() == Status.APPROVED;
                    case 3 -> v.status() == Status.REJECTED;
                    default -> v.status() != Status.SCHEDULED;
                }).toList());
        var filters = new TrevorSegmentedControl(List.of("Todas", "Pendientes", "Aprobadas", "Rechazadas"), index -> {
            selected.set(index);
            refresh.run();
        });
        var filtersLine = row(10, filters, spacer(), button("Nueva solicitud", "plus", "primary-button",
                () -> newRequest(refresh)));
        refresh.run();
        screen.add("filterSlot", filtersLine);
        screen.add("requestsSlot", new TrevorCard("white", heading("Solicitudes de acceso", null), table));
        return screen.view;
    }

    void newRequest(Runnable onCreated) {
        var name = field("Nombre completo", "");
        var document = field("Documento", "");
        var company = select("Empresa", data.companies.stream().map(Company::name).toList(), data.companies.getFirst().name());
        var date = SupervisorModules.date("Fecha");
        date.setValue(data.today);
        var time = field("Hora", "10:00");
        time.setPrefColumnCount(7);
        var reason = new TextArea();
        reason.setPrefRowCount(2);
        reason.setWrapText(true);
        var feedback = label("", "form-error");
        var content = new VBox(10, fieldGroup("Nombre completo", name), fieldGroup("Documento", document),
                fieldGroup("Empresa", company), row(12, fieldGroup("Fecha", date), fieldGroup("Hora (HH:mm)", time)),
                fieldGroup("Motivo de la visita", reason), feedback);
        TrevorDialog.open(shell.window(), "Nueva solicitud", "Completa los datos de la visita.", content,
                "Crear solicitud", () -> {
                    if (name.getText().isBlank() || document.getText().isBlank() || reason.getText().isBlank()
                            || date.getValue() == null || company.getValue() == null) {
                        feedback.setText("Completa todos los campos.");
                        return false;
                    }
                    try {
                        LocalTime selectedTime = LocalTime.parse(time.getText().strip(), DemoData.TIME);
                        Person existing = data.findPerson(document.getText());
                        if (existing != null && (!existing.name().equalsIgnoreCase(name.getText().strip())
                                || !existing.company().equals(company.getValue()))) {
                            feedback.setText("El documento ya está asociado a " + existing.name() + " de " + existing.company() + ".");
                            return false;
                        }
                        Person person = existing == null ? new Person(name.getText().strip(), document.getText().strip(),
                                company.getValue(), -1, Status.WAITING) : existing;
                        data.visits.addFirst(new Visit(person, reason.getText().strip(), date.getValue(),
                                selectedTime, Status.PENDING));
                        data.audit("Solicitud creada", shell.profile(), person.name() + " · " + person.company(), "Autorizaciones");
                        onCreated.run();
                        shell.notify("Solicitud creada", person.name() + " · Pendiente de autorización");
                        return true;
                    } catch (DateTimeParseException error) {
                        feedback.setText("Introduce una hora válida en formato HH:mm.");
                        return false;
                    }
                });
    }
}
