package com.trevor.sica.controllers;

import com.trevor.sica.components.branding.Icons;
import com.trevor.sica.components.branding.Portrait;
import com.trevor.sica.components.calendar.TrevorCalendar;
import com.trevor.sica.components.cards.TrevorCard;
import com.trevor.sica.components.cards.TrevorEmptyState;
import com.trevor.sica.components.cards.TrevorStatusBadge;
import com.trevor.sica.components.dialogs.TrevorDialog;
import com.trevor.sica.components.forms.TrevorSearchField;
import com.trevor.sica.demo.DemoData;
import com.trevor.sica.demo.DemoData.*;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

import static com.trevor.sica.components.TrevorUi.*;
import static com.trevor.sica.controllers.ModuleController.*;

final class EmployeeModules {
    private final ShellController shell;
    private final DemoData data;
    private final DashboardController dashboard;

    EmployeeModules(ShellController shell) {
        this.shell = shell;
        data = shell.data;
        dashboard = new DashboardController(shell);
    }

    Parent calendar() {
        var screen = new ScreenLayout("employee/calendar");
        var details = new VBox(10);
        var calendar = new TrevorCalendar(data.today, dashboard::scheduledCompany,
                day -> dashboard.dayAgenda(details, day, dashboard::scheduledCompany));
        dashboard.dayAgenda(details, data.today, dashboard::scheduledCompany);
        screen.add("calendarSlot", split(new TrevorCard("white", heading("Agenda de Empresa Sol", null), calendar),
                new TrevorCard("aqua", heading("Visitas del día", Icons.icon("calendar", 18)), details), 388));
        return screen.view;
    }

    Parent approvals() {
        var screen = new ScreenLayout("employee/approvals");
        var requests = screen.slot("requestsSlot");
        var search = new TrevorSearchField("Buscar visitante", text -> refreshApprovals(requests, text));
        screen.add("filterSlot", new TrevorCard("white", filterRow(search)));
        refreshApprovals(requests, "");
        return screen.view;
    }

    private void refreshApprovals(VBox requests, String query) {
        requests.getChildren().clear();
        Runnable refresh = () -> refreshApprovals(requests, query);
        data.companyVisits().stream().filter(visit -> visit.status() == Status.PENDING
                && contains(visit.person().name(), query)).forEach(visit -> {
            Person person = visit.person();
            var identity = row(11, new Portrait(person.name(), person.portrait(), 37),
                    textBlock(person.name(), person.company()), spacer(), new TrevorStatusBadge(visit.status()));
            var detail = textBlock(visit.reason(), SupervisorModules.stamp(visit.date(), visit.time()));
            var actions = row(8,
                    button("Rechazar", "close", "danger-button", () -> decide(visit, Status.REJECTED, refresh)),
                    button("Aprobar", "check", "primary-button", () -> decide(visit, Status.APPROVED, refresh)));
            requests.getChildren().add(new TrevorCard("white", identity, rule(), row(12, detail, spacer(), actions)));
        });
        if (requests.getChildren().isEmpty()) requests.getChildren().add(new TrevorEmptyState("Sin solicitudes pendientes",
                "Las solicitudes procesadas están disponibles en el historial."));
    }

    private void decide(Visit visit, Status status, Runnable refresh) {
        boolean approved = status == Status.APPROVED;
        String action = approved ? "Aprobar solicitud" : "Rechazar solicitud";
        TrevorDialog.open(shell.window(), action,
                approved ? "La visita quedará visible en el calendario." : "La decisión quedará visible en el historial.",
                new TrevorCard("soft", textBlock(visit.person().name(), visit.reason())), action, () -> {
                    data.visits.set(data.visits.indexOf(visit), visit.withStatus(status));
                    data.audit(approved ? "Autorización aprobada" : "Autorización rechazada", shell.profile(),
                            visit.person().name() + " · " + visit.reason(), "Autorizaciones");
                    refresh.run();
                    shell.notify(approved ? "Solicitud aprobada" : "Solicitud rechazada", visit.person().name());
                    return true;
                });
    }

    Parent history() {
        var screen = new ScreenLayout("employee/history");
        var table = visitTable();
        var statuses = List.of("Todos los estados", "Pendiente", "Aprobada", "Rechazada", "Programada");
        var status = select("Estado", statuses, statuses.getFirst());
        var from = SupervisorModules.date("Desde");
        var to = SupervisorModules.date("Hasta");
        var search = new TrevorSearchField("Buscar visitante", ignored -> {});
        Runnable refresh = () -> table.show(data.companyVisits().stream()
                .filter(visit -> inRange(visit.date(), from, to) && contains(visit.person().name(), search.query())
                        && (status.getValue().equals(statuses.getFirst()) || visit.status().label.equals(status.getValue())))
                .sorted(Comparator.comparing(Visit::date).thenComparing(Visit::time).reversed()).toList());
        search.onSubmit(refresh);
        status.setOnAction(event -> refresh.run());
        from.setOnAction(event -> refresh.run());
        to.setOnAction(event -> refresh.run());
        screen.add("filterSlot", new TrevorCard("white", filterRow(search, fieldGroup("Estado", status),
                fieldGroup("Desde", from), fieldGroup("Hasta", to), button("Aplicar", null, "primary-button", refresh))));
        screen.add("historySlot", new TrevorCard("white", heading("Solicitudes y visitas", null), table));
        refresh.run();
        return screen.view;
    }
}
