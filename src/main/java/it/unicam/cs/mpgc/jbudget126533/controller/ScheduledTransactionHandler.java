package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import it.unicam.cs.mpgc.jbudget126533.util.AlertManager;
import it.unicam.cs.mpgc.jbudget126533.util.FormValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce l'interfaccia utente e le operazioni relative alle transazioni programmate.
 * Fornisce metodi per aggiungere, rimuovere e verificare transazioni programmate, oltre
 * alla gestione della tabella e dei campi di input.
 */
public class ScheduledTransactionHandler extends BaseHandler<ScheduledTransaction> {

    private final TextField scheduledDescField;
    private final TextField scheduledAmountField;
    private final ChoiceBox<MovementType> scheduledType;
    private final ListView<ITag> scheduledTagsListView;
    private final ChoiceBox<RecurrenceType> scheduledRecurrence;
    private final DatePicker scheduledStartDate;
    private final DatePicker scheduledEndDate;
    private final TableView<ScheduledTransaction> scheduledTable;

    private final ObservableList<ScheduledTransaction> scheduledObservableList = FXCollections.observableArrayList();

    /**
     * Costruttore.
     *
     * @param ledger                 riferimento al {@link Ledger} per gestione transazioni
     * @param scheduledDescField     campo di testo per descrizione transazione
     * @param scheduledAmountField   campo di testo per importo transazione
     * @param scheduledType          ChoiceBox per tipo di movimento (SPESA/ENTRATA)
     * @param scheduledTagsListView  ListView per selezione tag
     * @param scheduledRecurrence    ChoiceBox per tipo di ricorrenza
     * @param scheduledStartDate     data di inizio
     * @param scheduledEndDate       data di fine
     * @param scheduledTable         tabella per visualizzazione delle transazioni programmate
     */
    public ScheduledTransactionHandler(Ledger ledger, TextField scheduledDescField,
                                       TextField scheduledAmountField, ChoiceBox<MovementType> scheduledType,
                                       ListView<ITag> scheduledTagsListView,
                                       ChoiceBox<RecurrenceType> scheduledRecurrence,
                                       DatePicker scheduledStartDate, DatePicker scheduledEndDate,
                                       TableView<ScheduledTransaction> scheduledTable) {
        super(ledger);
        this.scheduledDescField = scheduledDescField;
        this.scheduledAmountField = scheduledAmountField;
        this.scheduledType = scheduledType;
        this.scheduledTagsListView = scheduledTagsListView;
        this.scheduledRecurrence = scheduledRecurrence;
        this.scheduledStartDate = scheduledStartDate;
        this.scheduledEndDate = scheduledEndDate;
        this.scheduledTable = scheduledTable;

        configureScheduledTable();
    }

    /**
     * Inizializza i ChoiceBox per tipo di movimento e ricorrenza con valori predefiniti.
     */
    public void initializeChoiceBoxes() {
        scheduledType.getItems().addAll(MovementType.values());
        scheduledType.setValue(MovementType.SPESA);

        scheduledRecurrence.getItems().addAll(RecurrenceType.values());
        scheduledRecurrence.setValue(RecurrenceType.MENSILE);
    }

    /**
     * Aggiunge una nuova transazione programmata basata sui campi di input.
     *
     * @param event evento scatenante
     */
    public void addScheduledTransaction(ActionEvent event) {
        try {
            String description = scheduledDescField.getText().trim();
            if (description.isEmpty()) {
                AlertManager.showErrorAlert("Inserisci una descrizione!");
                return;
            }

            double amount = Double.parseDouble(scheduledAmountField.getText());
            MovementType type = scheduledType.getValue();
            RecurrenceType recurrence = scheduledRecurrence.getValue();
            LocalDate startDate = scheduledStartDate.getValue();
            LocalDate endDate = scheduledEndDate.getValue();

            List<ITag> selectedTags = new ArrayList<>(scheduledTagsListView.getSelectionModel().getSelectedItems());
            if (!FormValidator.validateTags(selectedTags, msg -> AlertManager.showWarningAlert("Attenzione", msg))) return;
            if (!FormValidator.validateStartDate(startDate, msg -> AlertManager.showErrorAlert("Errore", msg))) return;

            ScheduledTransaction transaction = new ScheduledTransaction(
                    description, amount, type, selectedTags, recurrence, startDate, endDate
            );

            ledger.addScheduledTransaction(transaction);
            refreshTable();
            clearInputFields();

            AlertManager.showInfoAlert("Transazione programmata aggiunta!");

        } catch (NumberFormatException e) {
            AlertManager.showErrorAlert("Importo non valido!");
        }
    }


    /**
     * Rimuove la transazione programmata selezionata nella tabella.
     *
     * @param event evento scatenante
     */
    public void removeScheduledTransaction(ActionEvent event) {
        executeOnSelectedItem(scheduledTable, this::deleteScheduledTransaction, "Seleziona una transazione da rimuovere!");
    }

    private void deleteScheduledTransaction(ScheduledTransaction transaction) {
        List<ScheduledTransaction> transactions = ledger.getScheduledTransactions();
        int index = transactions.indexOf(transaction);
        if (index != -1) {
            ledger.removeScheduledTransaction(index);
            refreshTable();
            AlertManager.showInfoAlert("Transazione programmata rimossa!");
        }
    }

    /**
     * Verifica tutte le transazioni programmate tramite il {@link Ledger}.
     *
     * @param event evento scatenante
     */
    public void checkScheduledTransactions(ActionEvent event) {
        ledger.checkScheduledTransactions();
        refreshTable();
    }

    /**
     * Aggiorna la tabella con le transazioni programmate correnti.
     */
    public void updateScheduledTable() {
        if (scheduledTable != null && ledger != null) {
            scheduledObservableList.clear();
            scheduledObservableList.addAll(ledger.getScheduledTransactions());
            scheduledTable.refresh();
        }
    }

    /**
     * Configura la tabella delle transazioni programmate con colonne e colorazioni condizionali.
     */
    private void configureScheduledTable() {
        try {
            if (!scheduledTable.getColumns().isEmpty()) {
                TableColumn<ScheduledTransaction, String> descColumn = (TableColumn<ScheduledTransaction, String>)
                        scheduledTable.getColumns().getFirst();
                descColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDescription()));
            }

            if (scheduledTable.getColumns().size() > 1) {
                TableColumn<ScheduledTransaction, String> amountColumn = (TableColumn<ScheduledTransaction, String>)
                        scheduledTable.getColumns().get(1);
                amountColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getAmount())));
            }

            if (scheduledTable.getColumns().size() > 2) {
                TableColumn<ScheduledTransaction, String> typeColumn = (TableColumn<ScheduledTransaction, String>)
                        scheduledTable.getColumns().get(2);
                typeColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getType().toString()));
            }

            if (scheduledTable.getColumns().size() > 3) {
                TableColumn<ScheduledTransaction, String> recurrenceColumn = (TableColumn<ScheduledTransaction, String>)
                        scheduledTable.getColumns().get(3);
                recurrenceColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getRecurrence().toString()));
            }

            if (scheduledTable.getColumns().size() > 4) {
                TableColumn<ScheduledTransaction, String> nextDateColumn = (TableColumn<ScheduledTransaction, String>)
                        scheduledTable.getColumns().get(4);
                nextDateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getNextExecutionDate().toString()));
            }

            scheduledTable.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(ScheduledTransaction transaction, boolean empty) {
                    super.updateItem(transaction, empty);
                    if (empty || transaction == null) {
                        setStyle("");
                    } else if (!transaction.isActive()) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else if (transaction.getNextExecutionDate().isBefore(LocalDate.now())) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            });

            scheduledTable.setItems(scheduledObservableList);

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella scheduled: " + e.getMessage());
        }
    }

    @Override
    public void refreshTable() {
        if (scheduledTable != null && ledger != null) {
            refreshTable(scheduledTable, ledger.getScheduledTransactions());
        }
    }

    @Override
    protected void clearInputFields() {
        scheduledDescField.clear();
        scheduledAmountField.clear();
        scheduledType.setValue(MovementType.SPESA);
        scheduledRecurrence.setValue(RecurrenceType.MENSILE);
        scheduledStartDate.setValue(null);
        scheduledEndDate.setValue(null);
        scheduledTagsListView.getSelectionModel().clearSelection();
    }
}
