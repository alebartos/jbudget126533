package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import it.unicam.cs.mpgc.jbudget126533.util.AlertManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.util.StringConverter;

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
public class TransactionHandler extends BaseHandler<ITransaction> {

    private final ChoiceBox<MovementType> typeTransaction;
    private final ChoiceBox<Person> personChoiceBox;
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
     * @param moneyTransaction      TextField per inserire l'importo
     * @param dateTransaction       DatePicker per selezionare la data
     * @param transactionTable      TableView per visualizzare le transazioni
     * @param transactionTagsListView ListView dei tag selezionabili
     * @param balanceLabel          Label per visualizzare il saldo
     */
    public TransactionHandler(Ledger ledger, ChoiceBox<MovementType> typeTransaction,
                              ChoiceBox<Person> personChoiceBox, TextField moneyTransaction,
                              DatePicker dateTransaction, TableView<ITransaction> transactionTable,
                              ListView<ITag> transactionTagsListView, Label balanceLabel) {
        super(ledger);
        this.typeTransaction = typeTransaction;
        this.personChoiceBox = personChoiceBox;
        this.moneyTransaction = moneyTransaction;
        this.dateTransaction = dateTransaction;
        this.transactionTable = transactionTable;
        this.transactionTagsListView = transactionTagsListView;
        this.balanceLabel = balanceLabel;

        configureTransactionTable();
    }

    /**
     * Aggiunge una nuova transazione al ledger.
     *
     * @param actionEvent evento dell'interfaccia grafica
     */
    public void addTransaction(ActionEvent actionEvent) {
        if (controlError()) {
            try {
                Person selectedPerson = personChoiceBox.getValue();
                if (selectedPerson == null) {
                    AlertManager.showErrorAlert("Seleziona una persona");
                    return;
                }

                String moneyText = moneyTransaction.getText();
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
                    AlertManager.showErrorAlert("Seleziona un Tag dalla lista!");
                    return;
                }

                ITransaction transaction = new Transaction(
                        typeTransaction.getValue(),
                        selectedPerson,
                        Double.parseDouble(moneyText),
                        dateTransaction.getValue() != null ? dateTransaction.getValue() : LocalDate.now(),
                        selectedTags
                );

                ledger.write(transaction);
                ledger.addTransaction(transaction);

                updateBalance();
                refreshTable();
                clearInputFields();
                transactionTagsListView.getSelectionModel().clearSelection();

                AlertManager.showInfoAlert("Transazione aggiunta con successo!");

            } catch (NumberFormatException e) {
                AlertManager.showErrorAlert("Formato importo non valido!");
            } catch (Exception e) {
                AlertManager.showErrorAlert("Si è verificato un errore: " + e.getMessage());
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
            AlertManager.showWarningAlert("Seleziona un tipo di transazione!");
            return false;
        }

        String moneyText = moneyTransaction.getText();
        if (!moneyText.matches("[\\-\\+]?[0-9]*(\\.[0-9]+)?")) {
            AlertManager.showErrorAlert("Inserisci un importo valido!");
            return false;
        }

        if (personChoiceBox.getValue() == null) {
            AlertManager.showWarningAlert("Seleziona una persona!");
            return false;
        }

        return true;
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
            if (!transactionTable.getColumns().isEmpty()) {
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
            if (transactionTable.getColumns().size() > 5) {
                TableColumn<ITransaction, Void> actionsColumn = (TableColumn<ITransaction, Void>)
                        transactionTable.getColumns().get(5);

                actionsColumn.setCellFactory(param -> new TableCell<ITransaction, Void>() {
                    private final Button deleteButton = new Button("Elimina");

                    {
                        deleteButton.setOnAction(event -> {
                            ITransaction transaction = getTableView().getItems().get(getIndex());
                            deleteTransaction(transaction);
                        });
                        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px;");
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella transazioni: " + e.getMessage());
        }
    }

    public void refreshPersonList() {
        if (personChoiceBox != null) {
            Person selectedPerson = personChoiceBox.getValue();
            personChoiceBox.setItems(FXCollections.observableArrayList(PersonManager.getAllPersons()));
            if (selectedPerson != null && PersonManager.getPerson(selectedPerson.getName()) != null) {
                personChoiceBox.setValue(selectedPerson);
            } else {
                personChoiceBox.setValue(null);
            }
        }
    }

    public void initializeChoiceBoxes() {
        typeTransaction.getItems().addAll(MovementType.values());
        typeTransaction.setValue(MovementType.SPESA);
        refreshPersonList();

        personChoiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Person person) {
                return person != null ? person.getName() : "Seleziona persona";
            }

            @Override
            public Person fromString(String string) {
                return PersonManager.getPerson(string);
            }
        });
    }

    public void deleteSelectedTransaction() {
        executeOnSelectedItem(transactionTable, this::deleteTransaction, "Seleziona una transazione da eliminare!");
    }

    private void deleteTransaction(ITransaction transaction) {
        boolean confirm = showConfirmationAlert(
                "Conferma eliminazione",
                "Eliminare la transazione?",
                "Transazione: " + transaction.getUser() + "\nImporto: " + transaction.getMoney() + "€"
        );

        if (confirm) {
            boolean removed = removeTransaction(transaction);
            if (removed) {
                AlertManager.showInfoAlert("Transazione eliminata con successo!");
                refreshTable();
                updateBalance();
            } else {
                AlertManager.showErrorAlert("Impossibile eliminare la transazione!");
            }
        }
    }

    private boolean removeTransaction(ITransaction transaction) {
        try {
            ArrayList<ITransaction> transactions = ledger.getTransaction();
            boolean removed = transactions.removeIf(t ->
                    t.getUser().equals(transaction.getUser()) &&
                            t.getMoney() == transaction.getMoney() &&
                            t.getDate().equals(transaction.getDate()) &&
                            t.getType() == transaction.getType()
            );

            if (removed) {
                ledger.setList(transactions);
                rewriteTransactionFile(transactions);
                return true;
            }
            return false;

        } catch (Exception e) {
            AlertManager.showErrorAlert("Errore durante l'eliminazione: " + e.getMessage());
            return false;
        }
    }

    private void rewriteTransactionFile(ArrayList<ITransaction> transactions) {
        try {
            IFileManagement fileManagement = new FileManagement();
            fileManagement.writeObject("Movement.json", transactions);
            System.out.println("File transazioni aggiornato con " + transactions.size() + " transazioni");
        } catch (Exception e) {
            System.err.println("Errore nel riscrivere il file: " + e.getMessage());
            throw new RuntimeException("Errore nel salvataggio delle transazioni", e);
        }
    }

    @Override
    public void refreshTable() {
        if (transactionTable != null) {
            refreshTable(transactionTable, ledger.getTransaction());
        }
    }

    @Override
    protected void clearInputFields() {
        personChoiceBox.setValue(null);
        moneyTransaction.clear();
        dateTransaction.setValue(LocalDate.now());
        typeTransaction.setValue(MovementType.SPESA);
        if (transactionTagsListView != null) {
            transactionTagsListView.getSelectionModel().clearSelection();
        }
    }
}
