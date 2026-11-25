package com.trevor.sica.components.forms;

import com.trevor.sica.components.cards.TrevorEmptyState;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

import java.util.Collection;
import java.util.function.Function;

public final class TrevorTable<T> extends TableView<T> {
    public TrevorTable() {
        getStyleClass().add("trevor-table");
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setPlaceholder(new TrevorEmptyState("Sin coincidencias", "Ajusta los filtros para consultar otros resultados."));
        setFixedCellSize(56);
        setMinWidth(0);
    }

    public void text(String title, double width, Function<T, String> value) {
        cells(title, width, item -> {
            String text = value.apply(item);
            var label = new javafx.scene.control.Label(text);
            if (text.length() > 20) label.setTooltip(new Tooltip(text));
            return label;
        });
    }

    public void cells(String title, double width, Function<T, Node> value) {
        TableColumn<T, T> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setMinWidth(Math.min(88, width));
        col.setSortable(false);
        col.setReorderable(false);
        col.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue()));
        col.setCellFactory(ignored -> new TableCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : value.apply(item));
            }
        });
        getColumns().add(col);
    }

    public void show(Collection<T> rows) {
        int count = Math.max(3, Math.min(9, rows.size()));
        getItems().setAll(rows);
        setPrefHeight(count * getFixedCellSize() + 43);
    }
}
