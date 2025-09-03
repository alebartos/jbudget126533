package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestisce le transazioni dell'utente nell'interfaccia grafica.
 * <p>
 * Le funzionalità principali includono:
 * <ul>
 *     <li>Aggiunta di transazioni</li>
 *     <li>Aggiornamento del saldo</li>
 *     <li>Visualizzazione delle transazioni in una TableView</li>
 *     <li>Gestione dei tag associati alle transazioni</li>
 * </ul>
 */
public class TransactionHandler {

    private final Ledger ledger;
    private final ChoiceBox<MovementType> typeTransaction;
    private final TextField userTransaction;
    private final TextField moneyTransaction;
    private final DatePicker dateTransaction;
    private final TableView<ITransaction> transactionTable;
    private final ListView<ITag> transactionTagsListView;
    private final Label balanceLabel;

    /**
     * Costruttore.
     *
     * @param ledger                ledger contenente le transazioni
     * @param typeTransaction       ChoiceBox per selezionare il tipo di transazione
     * @param userTransaction       TextField per inserire l'utente
     * @param moneyTransaction      TextField per inserire l'importo
     * @param dateTransaction       DatePicker per selezionare la data
     * @param transactionTable      TableView per visualizzare le transazioni
     * @param transactionTagsListView ListView dei tag selezionabili
     * @param balanceLabel          Label per visualizzare il saldo
     */
    public TransactionHandler(Ledger ledger, ChoiceBox<MovementType> typeTransaction,
                              TextField userTransaction, TextField moneyTransaction,
                              DatePicker dateTransaction, TableView<ITransaction> transactionTable,
                              ListView<ITag> transactionTagsListView, Label balanceLabel) {
        this.ledger = ledger;
        this.typeTransaction = typeTransaction;
        this.userTransaction = userTransaction;
        this.moneyTransaction = moneyTransaction;
        this.dateTransaction = dateTransaction;
        this.transactionTable = transactionTable;
        this.transactionTagsListView = transactionTagsListView;
        this.balanceLabel = balanceLabel;

        configureTransactionTable();
    }

    /**
     * Inizializza le ChoiceBox con i tipi di transazione disponibili.
     */
    public void initializeChoiceBoxes() {
        typeTransaction.getItems().addAll(MovementType.values());
        typeTransaction.setValue(MovementType.SPESA);
    }

    /**
     * Aggiunge una nuova transazione al ledger.
     *
     * @param actionEvent evento dell'interfaccia grafica
     */
    public void addTransaction(ActionEvent actionEvent) {
        if (controlError()) {
            try {
                String moneyText = moneyTransaction.getText();

                // Corregge il segno in base al tipo
                if (typeTransaction.getValue().equals(MovementType.SPESA) && !moneyText.startsWith("-")) {
                    moneyText = "-" + moneyText;
                } else if (typeTransaction.getValue().equals(MovementType.GUADAGNO) && moneyText.startsWith("-")) {
                    moneyText = moneyText.replace("-", "");
                }

                List<ITag> selectedTags = new ArrayList<>();
                if (transactionTagsListView != null) {
                    selectedTags.addAll(transactionTagsListView.getSelectionModel().getSelectedItems());
                }

                if (selectedTags.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona almeno un tag dalla lista!");
                    return;
                }

                ITransaction transaction = new Transaction(
                        typeTransaction.getValue(),
                        userTransaction.getText().toUpperCase(),
                        Double.parseDouble(moneyText),
                        dateTransaction.getValue() != null ? dateTransaction.getValue() : LocalDate.now(),
                        selectedTags
                );

                ledger.write(transaction);
                ledger.addTransaction(transaction);

                updateBalance();
                loadTransactionTable();
                clearInput();
                if (transactionTagsListView != null) {
                    transactionTagsListView.getSelectionModel().clearSelection();
                }

                showAlert(Alert.AlertType.INFORMATION, "Successo", "Transazione aggiunta con successo!");

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Formato importo non valido!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Controlla eventuali errori nei campi di input.
     *
     * @return true se non ci sono errori, false altrimenti
     */
    private boolean controlError() {
        if (typeTransaction.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Errore", "Seleziona un tipo di transazione!");
            return false;
        }

        String moneyText = moneyTransaction.getText();
        if (!moneyText.matches("[\\-\\+]?[0-9]*(\\.[0-9]+)?")) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci un importo valido!");
            return false;
        }

        if (userTransaction.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Inserisci un nome utente!");
            return false;
        }

        return true;
    }

    /**
     * Pulisce i campi di input dopo l'inserimento di una transazione.
     */
    private void clearInput() {
        userTransaction.clear();
        moneyTransaction.clear();
        dateTransaction.setValue(LocalDate.now());
        typeTransaction.setValue(MovementType.SPESA);
        if (transactionTagsListView != null) {
            transactionTagsListView.getSelectionModel().clearSelection();
        }
    }

    /**
     * Aggiorna il saldo visualizzato.
     */
    public void updateBalance() {
        balanceLabel.setText(String.format("%.2f €", ledger.getBalance()));
    }

    /**
     * Carica le transazioni nella TableView.
     */
    public void loadTransactionTable() {
        if (transactionTable != null) {
            transactionTable.setItems(FXCollections.observableList(ledger.getTransaction()));
        }
    }

    /**
     * Configura le colonne della TableView delle transazioni.
     */
    private void configureTransactionTable() {
        try {
            if (transactionTable.getColumns().size() > 0) {
                TableColumn<ITransaction, String> typeColumn = (TableColumn<ITransaction, String>)
                        transactionTable.getColumns().getFirst();
                typeColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getType().toString()));
            }

            if (transactionTable.getColumns().size() > 1) {
                TableColumn<ITransaction, String> userColumn = (TableColumn<ITransaction, String>)
                        transactionTable.getColumns().get(1);
                userColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getUser()));
            }

            if (transactionTable.getColumns().size() > 2) {
                TableColumn<ITransaction, String> moneyColumn = (TableColumn<ITransaction, String>)
                        transactionTable.getColumns().get(2);
                moneyColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getMoney())));

                moneyColumn.setCellFactory(column -> new TableCell<>() {
                    @Override
                    protected void updateItem(String amountText, boolean empty) {
                        super.updateItem(amountText, empty);
                        if (empty || amountText == null) {
                            setText(null);
                            setTextFill(javafx.scene.paint.Color.BLACK);
                        } else {
                            setText(amountText);
                            ITransaction transaction = getTableView().getItems().get(getIndex());
                            setTextFill(transaction.getMoney() < 0 ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
                        }
                    }
                });
            }

            if (transactionTable.getColumns().size() > 3) {
                TableColumn<ITransaction, String> dateColumn = (TableColumn<ITransaction, String>)
                        transactionTable.getColumns().get(3);
                dateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDate().toString()));
            }

            if (transactionTable.getColumns().size() > 4) {
                TableColumn<ITransaction, String> tagColumn = (TableColumn<ITransaction, String>)
                        transactionTable.getColumns().get(4);
                tagColumn.setCellValueFactory(cellData -> {
                    ITransaction transaction = cellData.getValue();
                    String tags = transaction.getTags().stream()
                            .map(ITag::getName)
                            .collect(Collectors.joining(", "));
                    return new SimpleStringProperty(tags);
                });
            }

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella transazioni: " + e.getMessage());
        }
    }

    /**
     * Mostra un alert grafico.
     *
     * @param type    tipo di alert
     * @param title   titolo dell'alert
     * @param message messaggio dell'alert
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
