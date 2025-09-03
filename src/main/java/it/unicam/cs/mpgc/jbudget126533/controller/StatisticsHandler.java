package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
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

            // Calcola i componenti separatamente
            double realTransactions = calculateRealTransactions(type, startDate, endDate);
            double scheduledTransactions = ledger.calculateScheduledTransactionsForPeriod(type, startDate, endDate);
            double amortizationPayments = 0;

            // Solo per le spese includi gli ammortamenti
            if (type == null || type == MovementType.SPESA) {
                amortizationPayments = calculateAmortizationPayments(startDate, endDate);
            }

            double result = realTransactions + scheduledTransactions + amortizationPayments;

            // Crea il messaggio dettagliato
            StringBuilder details = new StringBuilder();
            details.append(String.format("%.2f €", result));

            if (scheduledTransactions != 0) {
                details.append(String.format(" (Prog: %.2f €", scheduledTransactions));

                if (amortizationPayments != 0) {
                    details.append(String.format(", Amm: %.2f € )", amortizationPayments));
                }

            } else if (amortizationPayments != 0) {
                details.append(String.format(" (Ammortamenti: %.2f €)", amortizationPayments));
            }

            balanceForRange.setText(details.toString());

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

            // USA I METODI CORRETTI:
            double realTrend = calculateRealTransactions(null, startDate, endDate);
            double scheduledTrend = ledger.calculateScheduledTransactionsForPeriod(null, startDate, endDate);
            double amortizationTrend = ledger.calculateAmortizationPaymentsForPeriod(startDate, endDate); // METODO CORRETTO

            double totalTrend = realTrend + scheduledTrend + amortizationTrend;
            String trend = totalTrend > 0 ? "📈 Positivo" : "📉 Negativo";

            // Conta usando i metodi corretti
            int realCount = countRealTransactions(null, startDate, endDate);
            int scheduledCount = countScheduledTransactions(null, startDate, endDate);
            int amortizationCount = ledger.countAmortizationPaymentsForPeriod(startDate, endDate);

            // Visualizzazione compatta per spazio limitato
            StringBuilder details = new StringBuilder();
            details.append(String.format("%s: %.2f €\n", trend, totalTrend));

            // Aggiungi icone e conteggi compatti
            if (realCount > 0 || scheduledCount > 0 || amortizationCount > 0) {

                if (realCount > 0) {
                    details.append(String.format("Reali: %.2f € (%d transazioni)\n", realTrend, realCount));
                }

                if (scheduledCount > 0) {
                    if (realCount > 0) details.append(String.format("Programmati: %.2f € (%d transazioni)\n", scheduledTrend, scheduledCount));
                }

                if (amortizationCount > 0) {
                    if (realCount > 0 || scheduledCount > 0)  details.append(String.format("Ammortamenti: %.2f € (%d rate)", amortizationTrend, amortizationCount));
                }

            }

            balanceTrend.setText(details.toString());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore nel calcolo: " + e.getMessage());
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
     * Calcola le transazioni reali per il periodo.
     */
    private double calculateRealTransactions(MovementType type, LocalDate startDate, LocalDate endDate) {
        return ledger.getTransaction().stream()
                .filter(t -> type == null || t.getType().equals(type))
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .mapToDouble(ITransaction::getMoney)
                .sum();
    }

    /**
     * Conta le transazioni reali nel periodo.
     */
    private int countRealTransactions(MovementType type, LocalDate startDate, LocalDate endDate) {
        return (int) ledger.getTransaction().stream()
                .filter(t -> type == null || t.getType().equals(type))
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .count();
    }

    /**
     * Conta le transazioni programmate nel periodo.
     */
    private int countScheduledTransactions(MovementType type, LocalDate startDate, LocalDate endDate) {
        try {
            return (int) ledger.getScheduledTransactions().stream()
                    .filter(st -> (type == null || st.getType().equals(type)) && st.isActive())
                    .filter(st -> st.getNextExecutionDate() != null &&
                            !st.getNextExecutionDate().isBefore(startDate) &&
                            !st.getNextExecutionDate().isAfter(endDate))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Popola la tabella dei saldi per ciascun tag in base al tipo di movimento selezionato.
     * Questo metodo è richiamato dal GUIController.
     *
     * @param actionEvent evento di azione
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
     * Calcola i pagamenti di ammortamento per il periodo.
     * Restituisce un valore NEGATIVO perché sono spese.
     */
    private double calculateAmortizationPayments(LocalDate startDate, LocalDate endDate) {
        try {
            return ledger.calculateAmortizationPaymentsForPeriod(startDate, endDate);
        } catch (Exception e) {
            System.err.println("Errore nel calcolo ammortamenti: " + e.getMessage());
            return 0;
        }
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
