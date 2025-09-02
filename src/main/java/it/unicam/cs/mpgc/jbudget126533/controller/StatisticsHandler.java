package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import it.unicam.cs.mpgc.jbudget126533.util.Pair;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestisce le statistiche del bilancio all'interno dell'interfaccia utente.
 * Fornisce metodi per:
 * <ul>
 *     <li>Calcolare il saldo per un intervallo di date</li>
 *     <li>Determinare il trend del bilancio</li>
 *     <li>Visualizzare il bilancio aggregato per tag</li>
 * </ul>
 */
public class StatisticsHandler {

    private final Ledger ledger;
    private final Label balanceForRange;
    private final DatePicker dateStartForRange;
    private final DatePicker dateEndForRange;
    private final ChoiceBox<MovementType> choiceForRange;
    private final Label balanceTrend;
    private final DatePicker dateStartForTrend;
    private final DatePicker dateEndForTrend;
    private final TableView<Map.Entry<String, Double>> tagTable;
    private final ChoiceBox<MovementType> choiceTypeForEachTag;

    /**
     * Costruttore.
     *
     * @param ledger                riferimento al {@link Ledger} per il calcolo delle statistiche
     * @param balanceForRange       Label per visualizzare il saldo su intervallo di date
     * @param dateStartForRange     Data di inizio intervallo
     * @param dateEndForRange       Data di fine intervallo
     * @param choiceForRange        ChoiceBox per selezionare il tipo di movimento (entrata/spesa)
     * @param balanceTrend          Label per visualizzare il trend del bilancio
     * @param dateStartForTrend     Data di inizio periodo per trend
     * @param dateEndForTrend       Data di fine periodo per trend
     * @param tagTable              Tabella per visualizzare saldo per tag
     * @param choiceTypeForEachTag  ChoiceBox per selezionare il tipo di movimento per la tabella tag
     */
    public StatisticsHandler(Ledger ledger, Label balanceForRange, DatePicker dateStartForRange,
                             DatePicker dateEndForRange, ChoiceBox<MovementType> choiceForRange,
                             Label balanceTrend, DatePicker dateStartForTrend, DatePicker dateEndForTrend,
                             TableView<Map.Entry<String, Double>> tagTable,
                             ChoiceBox<MovementType> choiceTypeForEachTag) {
        this.ledger = ledger;
        this.balanceForRange = balanceForRange;
        this.dateStartForRange = dateStartForRange;
        this.dateEndForRange = dateEndForRange;
        this.choiceForRange = choiceForRange;
        this.balanceTrend = balanceTrend;
        this.dateStartForTrend = dateStartForTrend;
        this.dateEndForTrend = dateEndForTrend;
        this.tagTable = tagTable;
        this.choiceTypeForEachTag = choiceTypeForEachTag;

        configureTagTable();
    }

    /**
     * Inizializza le ChoiceBox dei tipi di movimento.
     */
    public void initializeChoiceBoxes() {
        choiceForRange.getItems().addAll(MovementType.values());
        choiceForRange.getItems().add(0, null);
        choiceForRange.setValue(null);

        choiceTypeForEachTag.getItems().addAll(MovementType.values());
        choiceTypeForEachTag.setValue(MovementType.SPESA);
    }

    /**
     * Aggiorna la Label con il saldo totale per un intervallo di date selezionato.
     * Include anche il dettaglio delle transazioni programmate.
     *
     * @param actionEvent evento scatenante
     */
    public void updateBalanceForRange(ActionEvent actionEvent) {
        try {
            LocalDate startDate = dateStartForRange.getValue();
            LocalDate endDate = dateEndForRange.getValue();
            MovementType type = choiceForRange.getValue();

            if (startDate == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona una data di inizio!");
                return;
            }

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            double result = ledger.balanceForDates(type, startDate, endDate);
            double scheduledAmount = ledger.calculateScheduledTransactionsForPeriod(type, startDate, endDate);

            if (scheduledAmount != 0) {
                balanceForRange.setText(String.format("%.2f € (di cui %.2f € programmati)", result, scheduledAmount));
            } else {
                balanceForRange.setText(String.format("%.2f €", result));
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore nel calcolo: " + e.getMessage());
        }
    }

    /**
     * Aggiorna la Label con il trend del bilancio tra due date.
     * Mostra il totale, i valori reali e quelli programmati.
     *
     * @param actionEvent evento scatenante
     */
    public void updateBalanceTrend(ActionEvent actionEvent) {
        try {
            LocalDate startDate = dateStartForTrend.getValue();
            LocalDate endDate = dateEndForTrend.getValue();

            if (startDate == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona una data di inizio!");
                return;
            }

            if (endDate == null) {
                endDate = LocalDate.now();
            }

            Pair<Boolean, Double> trendResult = ledger.trendBalance(startDate, endDate).apply(ledger.getTransaction());
            double realTrend = trendResult.getSecond();
            double scheduledTrend = ledger.calculateScheduledTransactionsForPeriod(null, startDate, endDate);

            double totalTrend = realTrend + scheduledTrend;
            String trend = totalTrend > 0 ? "📈 Positivo" : "📉 Negativo";

            if (scheduledTrend != 0) {
                balanceTrend.setText(String.format("%s (Totale: %.2f € | Reali: %.2f € | Programmati: %.2f €)",
                        trend, totalTrend, realTrend, scheduledTrend));
            } else {
                balanceTrend.setText(String.format("%s (%.2f €)", trend, totalTrend));
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore nel calcolo: " + e.getMessage());
        }
    }

    /**
     * Popola la tabella dei saldi per ciascun tag in base al tipo di movimento selezionato.
     *
     * @param actionEvent evento scatenante
     */
    public void showTypeTagTable(ActionEvent actionEvent) {
        try {
            MovementType type = choiceTypeForEachTag.getValue();
            HashMap<String, Double> hashMap = ledger.balanceForEachTag(type);

            List<Map.Entry<String, Double>> sortedEntries = hashMap.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(Math.abs(e2.getValue()), Math.abs(e1.getValue())))
                    .collect(Collectors.toList());

            ObservableList<Map.Entry<String, Double>> items = FXCollections.observableArrayList(sortedEntries);
            tagTable.setItems(items);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile generare la tabella: " + e.getMessage());
        }
    }

    /**
     * Configura la tabella dei saldi per tag con le colonne e formattazione dell'importo.
     */
    private void configureTagTable() {
        TableColumn<Map.Entry<String, Double>, String> tagColumn = (TableColumn<Map.Entry<String, Double>, String>)
                tagTable.getColumns().get(0);
        TableColumn<Map.Entry<String, Double>, Double> amountColumn = (TableColumn<Map.Entry<String, Double>, Double>)
                tagTable.getColumns().get(1);

        tagColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getKey()));
        amountColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getValue()).asObject());

        amountColumn.setCellFactory(column -> new TableCell<Map.Entry<String, Double>, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f €", amount));
                }
            }
        });
    }

    /**
     * Mostra un alert grafico.
     *
     * @param type    tipo di alert
     * @param title   titolo
     * @param message messaggio
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
