package com.trevor.sica.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

public final class DemoData {
    public static final Locale SPANISH = Locale.forLanguageTag("es-CO");
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", SPANISH);
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT);
    public final LocalDate today = LocalDate.now(ZoneId.of("America/Bogota"));

    public enum Route {
        HOME("Inicio", "home", ""), RECORDS("Registros", "scan", "Consulta un documento y registra un movimiento."),
        REQUESTS("Solicitudes", "file", "Consulta y gestiona las solicitudes de acceso."),
        VISITS("Consulta de visitas", "search", "Revisa la información de una visita y sus incidentes."),
        RESTRICTIONS("Restricciones", "shield", "Consulta el estado de acceso de una persona."),
        INCIDENTS("Historial de incidentes", "warning", "Consulta las novedades registradas en Zona ACME."),
        AUDIT("Auditoría", "audit", "Explora la actividad de Zona ACME por tipo de acción y fecha."),
        COMPANIES("Empresas", "building", "Consulta las empresas y los funcionarios asociados."),
        ACCOUNTS("Cuentas", "users", "Gestiona las cuentas de demostración y sus perfiles."),
        ROLES("Roles y permisos", "key", "Explora los permisos que corresponden a cada perfil."),
        CALENDAR("Calendario de visitas", "calendar", "Consulta la agenda de visitas por día."),
        APPROVALS("Autorizaciones", "check-circle", "Revisa las solicitudes de acceso de tu empresa."),
        HISTORY("Historial", "audit", "Consulta las solicitudes y visitas de tu empresa.");

        public final String title;
        public final String icon;
        public final String description;

        Route(String title, String icon, String description) {
            this.title = title;
            this.icon = icon;
            this.description = description;
        }
    }

    public enum Profile {
        GUARD("Guarda de seguridad", "Carlos Ruiz", "Operación de accesos", 0,
                Route.HOME, Route.RECORDS, Route.REQUESTS),
        SUPERVISOR("Supervisor de seguridad", "Andrés Pérez", "Monitoreo y seguridad", 1,
                Route.HOME, Route.VISITS, Route.RESTRICTIONS, Route.INCIDENTS, Route.AUDIT),
        ADMIN("Superusuario", "Administrador SICA", "Administración de Zona ACME", 2,
                Route.HOME, Route.COMPANIES, Route.ACCOUNTS, Route.ROLES, Route.AUDIT),
        EMPLOYEE("Funcionario de empresa", "Valentina Mora", "Empresa Sol", 3,
                Route.HOME, Route.CALENDAR, Route.APPROVALS, Route.HISTORY);

        public final String title;
        public final String name;
        public final String brief;
        public final int portrait;
        public final List<Route> routes;

        Profile(String title, String name, String brief, int portrait, Route... routes) {
            this.title = title;
            this.name = name;
            this.brief = brief;
            this.portrait = portrait;
            this.routes = List.of(routes);
        }

        @Override public String toString() { return title; }
    }

    public enum Status {
        AUTHORIZED("Acceso autorizado", "positive"), RESTRICTED("Acceso restringido", "negative"),
        WAITING("Visita pendiente", "warning"), PENDING("Pendiente", "warning"),
        APPROVED("Aprobada", "positive"), REJECTED("Rechazada", "negative"),
        SCHEDULED("Programada", "neutral"), ENTRY("Entrada", "positive"),
        EXIT("Salida", "neutral"), WORK("Ingreso laboral", "positive"),
        ACTIVE("Activa", "positive"), INACTIVE("Inactiva", "neutral"),
        LOGGED("Registrado", "neutral");

        public final String label;
        public final String tone;
        Status(String label, String tone) { this.label = label; this.tone = tone; }
    }

    public record Person(String name, String document, String company, int portrait, Status access) {}
    public record Visit(Person person, String reason, LocalDate date, LocalTime time, Status status) {
        public Visit withStatus(Status value) { return new Visit(person, reason, date, time, value); }
    }
    public record Movement(Person person, LocalDate date, LocalTime time, Status type) {}
    public record Audit(String action, String actor, String detail, LocalDate date, LocalTime time, String type) {}
    public record Incident(Visit visit, LocalDate date, LocalTime time, String description) {}
    public record Account(String name, String email, Profile role, String company, Status status, int portrait) {}
    public record Company(String name, String officer, Status status, String mark) {
        public Company withStatus(Status value) { return new Company(name, officer, value, mark); }
    }

    public final Person laura = new Person("Laura Méndez", "102030", "Empresa Sol", -1, Status.AUTHORIZED);
    public final Person diego = new Person("Diego Ramírez", "204060", "Empresa Luna", -1, Status.RESTRICTED);
    public final Person camila = new Person("Camila Torres", "306090", "Empresa Sol", -1, Status.WAITING);
    public final Person andres = new Person("Andrés Pérez", "708090", "Zona ACME", 1, Status.AUTHORIZED);
    public final Person mateo = new Person("Mateo Vargas", "405060", "Empresa Luna", -1, Status.AUTHORIZED);
    public final Person sofia = new Person("Sofía Herrera", "516070", "Empresa Sol", -1, Status.AUTHORIZED);
    public final Person natalia = new Person("Natalia López", "617080", "Empresa Sol", -1, Status.WAITING);
    public final Person juan = new Person("Juan Medina", "718090", "Empresa Sol", -1, Status.WAITING);
    public final Person elena = new Person("Elena Silva", "819090", "Empresa Luna", -1, Status.WAITING);
    public final List<Person> directory = List.of(laura, diego, camila, andres, mateo, sofia, natalia, juan, elena);

    public final ObservableList<Visit> visits = FXCollections.observableArrayList(
            new Visit(laura, "Reunión de seguimiento", today, LocalTime.of(10, 30), Status.SCHEDULED),
            new Visit(mateo, "Entrega de documentación", today, LocalTime.of(11, 15), Status.SCHEDULED),
            new Visit(sofia, "Visita técnica", today, LocalTime.of(14, 0), Status.SCHEDULED),
            new Visit(sofia, "Seguimiento técnico", today.plusDays(2), LocalTime.of(9, 0), Status.SCHEDULED),
            new Visit(laura, "Reunión comercial", today.plusDays(5), LocalTime.of(14, 0), Status.SCHEDULED),
            new Visit(camila, "Reunión con operaciones", today, LocalTime.of(15, 0), Status.PENDING),
            new Visit(mateo, "Mantenimiento programado", today.plusDays(1), LocalTime.of(10, 0), Status.PENDING),
            new Visit(natalia, "Entrega de equipos", today.plusDays(1), LocalTime.of(11, 0), Status.PENDING),
            new Visit(juan, "Revisión de proveedores", today.plusDays(2), LocalTime.of(10, 30), Status.PENDING),
            new Visit(elena, "Entrega de contratos", today.plusDays(2), LocalTime.of(13, 0), Status.PENDING),
            new Visit(sofia, "Inspección de instalaciones", today.plusDays(3), LocalTime.of(8, 30), Status.PENDING),
            new Visit(laura, "Instalación de equipos", today.plusDays(4), LocalTime.of(16, 0), Status.PENDING),
            new Visit(camila, "Entrega de credenciales", today.plusDays(5), LocalTime.of(14, 0), Status.PENDING),
            new Visit(andres, "Inspección de seguridad", today.minusDays(1), LocalTime.of(8, 0), Status.APPROVED),
            new Visit(diego, "Visita de proveedor", today.minusDays(2), LocalTime.of(9, 15), Status.REJECTED));

    public final ObservableList<Movement> movements = FXCollections.observableArrayList(
            new Movement(laura, today, LocalTime.of(9, 42), Status.ENTRY),
            new Movement(mateo, today, LocalTime.of(9, 36), Status.EXIT),
            new Movement(andres, today, LocalTime.of(9, 18), Status.WORK),
            new Movement(sofia, today, LocalTime.of(8, 54), Status.ENTRY));

    public final ObservableList<Audit> audits = FXCollections.observableArrayList(
            new Audit("Entrada registrada", "Carlos Ruiz", "Laura Méndez · Empresa Sol", today, LocalTime.of(9, 42), "Registros"),
            new Audit("Incidente registrado", "Andrés Pérez", "Visita de Laura Méndez", today, LocalTime.of(9, 20), "Seguridad"),
            new Audit("Cuenta actualizada", "Administrador SICA", "Valentina Mora · Empresa Sol", today, LocalTime.of(8, 50), "Administración"),
            new Audit("Inicio de sesión", "Andrés Pérez", "Supervisor de seguridad", today, LocalTime.of(8, 12), "Sesiones"),
            new Audit("Autorización aprobada", "Valentina Mora", "Inspección de seguridad", today.minusDays(1), LocalTime.of(16, 30), "Autorizaciones"));

    public final ObservableList<Incident> incidents = FXCollections.observableArrayList(
            new Incident(visits.getFirst(), today, LocalTime.of(9, 20), "Se verificó la documentación con la empresa anfitriona."),
            new Incident(visits.getLast(), today.minusDays(2), LocalTime.of(9, 20), "Se derivó la consulta al supervisor de turno."));

    public final ObservableList<Account> accounts = FXCollections.observableArrayList(
            new Account("Carlos Ruiz", "carlos.ruiz@example.com", Profile.GUARD, "Zona ACME", Status.ACTIVE, 0),
            new Account("Andrés Pérez", "andres.perez@example.com", Profile.SUPERVISOR, "Zona ACME", Status.ACTIVE, 1),
            new Account("Administrador SICA", "admin.sica@example.com", Profile.ADMIN, "Zona ACME", Status.ACTIVE, 2),
            new Account("Valentina Mora", "valentina.mora@example.com", Profile.EMPLOYEE, "Empresa Sol", Status.ACTIVE, 3),
            new Account("Julián Rojas", "julian.rojas@example.com", Profile.EMPLOYEE, "Empresa Luna", Status.INACTIVE, -1));

    public final ObservableList<Company> companies = FXCollections.observableArrayList(
            new Company("Empresa Sol", "Valentina Mora", Status.ACTIVE, "sun"),
            new Company("Empresa Luna", "Julián Rojas", Status.ACTIVE, "moon"));

    public static final int[] WEEKLY_ENTRIES = {27, 43, 37, 59, 43, 61, 52};
    public static final int[] WEEKLY_EXITS = {20, 28, 24, 37, 26, 48, 32};

    public static final int[] HOURLY_ENTRIES = {8, 19, 23, 15, 8, 7, 5};
    public static final int[] HOURLY_EXITS = {3, 7, 9, 8, 6, 5, 5};

    public int movementCount(Status type) {
        int illustratedTotal = type == Status.EXIT ? 42 : 82;
        return illustratedTotal + (int) movements.stream()
                .filter(m -> type == Status.ENTRY ? m.type != Status.EXIT : m.type == type).count();
    }

    public Person findPerson(String document) {
        return directory.stream().filter(person -> person.document.equals(document.strip())).findFirst()
                .orElseGet(() -> visits.stream().map(Visit::person)
                        .filter(person -> person.document.equals(document.strip())).findFirst().orElse(null));
    }

    public Status accessStatus(Person person) {
        if (person.access != Status.WAITING) return person.access;
        return visits.stream().anyMatch(visit -> visit.person.equals(person) && visit.date.equals(today)
                && visit.status == Status.APPROVED) ? Status.AUTHORIZED : Status.WAITING;
    }

    public List<Visit> companyVisits() {
        return visits.stream().filter(v -> v.person.company.equals(Profile.EMPLOYEE.brief)).toList();
    }

    public void audit(String action, Profile actor, String detail, String type) {
        audits.addFirst(new Audit(action, actor.name, detail, today, currentTime(), type));
    }

    public LocalTime currentTime() { return LocalTime.now(ZoneId.of("America/Bogota")).withSecond(0).withNano(0); }
}
