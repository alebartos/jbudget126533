package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.Budget;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Gestisce le operazioni relative ai budget assegnati alle categorie/tag.
 * Fornisce funzionalità per:
 * <ul>
 *     <li>Impostare nuovi budget</li>
 *     <li>Aggiornare i budget esistenti</li>
 *     <li>Eliminare budget selezionati</li>
 *     <li>Controllare il superamento dei budget e notificare l'utente</li>
 *     <li>Visualizzare i budget in una tabella con colorazione condizionale</li>
 * </ul>
 * Questa classe è strettamente legata all'interfaccia JavaFX.
 */
public class BudgetHandler {

    private final Ledger ledger;
    private final ListView<ITag> budgetTagsListView;
    private final TextField budgetAmountField;
    private final DatePicker budgetStartDate;
    private final DatePicker budgetEndDate;
    private final TableView<Budget> budgetTable;

    private final ObservableList<Budget> budgetObservableList = FXCollections.observableArrayList();

    /**
     * Costruttore della classe.
     *
     * @param ledger            il registro principale che gestisce i dati
     * @param budgetTagsListView lista dei tag selezionabili
     * @param budgetAmountField  campo di input per l'importo del budget
     * @param budgetStartDate    campo di input per la data di inizio
     * @param budgetEndDate      campo di input per la data di fine
     * @param budgetTable        tabella dei budget
     */
    public BudgetHandler(Ledger ledger, ListView<ITag> budgetTagsListView,
                         TextField budgetAmountField, DatePicker budgetStartDate,
                         DatePicker budgetEndDate, TableView<Budget> budgetTable) {
        this.ledger = ledger;
        this.budgetTagsListView = budgetTagsListView;
        this.budgetAmountField = budgetAmountField;
        this.budgetStartDate = budgetStartDate;
        this.budgetEndDate = budgetEndDate;
        this.budgetTable = budgetTable;

        configureBudgetTable();
    }

    /**
     * Imposta un budget per il tag selezionato utilizzando i dati inseriti
     * nei campi dell'interfaccia.
     *
     * @param event evento di azione (es. click su bottone)
     * @throws NumberFormatException se l'importo non è un numero valido
     */
    public void setBudget(ActionEvent event) {
        try {
            ITag selectedTag = budgetTagsListView.getSelectionModel().getSelectedItem();
            if (selectedTag == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona un tag dalla lista!");
                return;
            }

            double amount = Double.parseDouble(budgetAmountField.getText());
            LocalDate startDate = budgetStartDate.getValue();
            LocalDate endDate = budgetEndDate.getValue();

            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona le date!");
                return;
            }

            ledger.setBudget(selectedTag.getName(), amount, startDate, endDate);
            updateBudgetTable();
            clearBudgetFields();

            showAlert(Alert.AlertType.INFORMATION, "Successo",
                    "Budget impostato per: " + selectedTag.getName());

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Importo non valido!");
        }
    }

    /**
     * Aggiorna tutti i budget nel ledger e nella tabella e controlla eventuali superamenti.
     *
     * @param event evento di azione
     */
    public void updateBudgets(ActionEvent event) {
        ledger.updateBudgets();
        updateBudgetTable();
        checkBudgetAlerts();
    }

    /**
     * Aggiorna la tabella dei budget con i dati correnti del ledger.
     */
    public void updateBudgetTable() {
        if (budgetTable != null && ledger != null) {
            budgetObservableList.clear();
            budgetObservableList.addAll(ledger.getAllBudgets().values());
            budgetTable.refresh();
        }
    }

    /**
     * Aggiorna la tabella dei budget e mostra un messaggio di pulizia dei budget scaduti.
     *
     * @param event evento di azione
     */
    public void cleanupBudgets(ActionEvent event) {
        updateBudgetTable();
        showAlert(Alert.AlertType.INFORMATION, "Pulizia", "Budget scaduti rimossi!");
    }

    /**
     * Rimuove il budget selezionato dalla tabella previa conferma dell'utente.
     *
     * @param event evento di azione
     */
    public void removeBudget(ActionEvent event) {
        Budget selectedBudget = budgetTable.getSelectionModel().getSelectedItem();
        if (selectedBudget != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Conferma eliminazione");
            confirmation.setHeaderText("Eliminare il budget per '" + selectedBudget.getCategory() + "'?");
            confirmation.setContentText("Questa operazione non può essere annullata.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                budgetTable.getItems().remove(selectedBudget);
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Budget eliminato!");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona un budget da eliminare!");
        }
    }

    /**
     * Controlla quali budget sono stati superati e mostra un alert con i dettagli.
     */
    private void checkBudgetAlerts() {
        List<Budget> exceededBudgets = ledger.getExceededBudgets();
        if (!exceededBudgets.isEmpty()) {
            StringBuilder message = new StringBuilder("Budget superati:\n");
            for (Budget budget : exceededBudgets) {
                message.append("- ").append(budget.getCategory())
                        .append(": ").append(String.format("%.2f€", budget.getSpentAmount()))
                        .append("/").append(String.format("%.2f€", budget.getAllocatedAmount()))
                        .append("\n");
            }
            showAlert(Alert.AlertType.WARNING, "Attenzione", message.toString());
        }
    }

    /**
     * Pulisce tutti i campi di input del form di inserimento budget.
     */
    private void clearBudgetFields() {
        budgetAmountField.clear();
        budgetStartDate.setValue(null);
        budgetEndDate.setValue(null);
    }

    /**
     * Configura le colonne della tabella dei budget, la formattazione dei valori e
     * la colorazione condizionale delle righe in base all'utilizzo del budget.
     */
    private void configureBudgetTable() {
        try {
            if (budgetTable.getColumns().size() > 0) {
                TableColumn<Budget, String> categoryColumn = (TableColumn<Budget, String>)
                        budgetTable.getColumns().get(0);
                categoryColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getCategory()));
            }

            if (budgetTable.getColumns().size() > 1) {
                TableColumn<Budget, String> allocatedColumn = (TableColumn<Budget, String>)
                        budgetTable.getColumns().get(1);
                allocatedColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getAllocatedAmount())));
            }

            if (budgetTable.getColumns().size() > 2) {
                TableColumn<Budget, String> spentColumn = (TableColumn<Budget, String>)
                        budgetTable.getColumns().get(2);
                spentColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getSpentAmount())));
            }

            if (budgetTable.getColumns().size() > 3) {
                TableColumn<Budget, String> remainingColumn = (TableColumn<Budget, String>)
                        budgetTable.getColumns().get(3);
                remainingColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f €", cellData.getValue().getRemainingAmount())));
            }

            if (budgetTable.getColumns().size() > 4) {
                TableColumn<Budget, String> percentageColumn = (TableColumn<Budget, String>)
                        budgetTable.getColumns().get(4);
                percentageColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.1f %%", cellData.getValue().getUsagePercentage())));
            }

            budgetTable.setRowFactory(tv -> new TableRow<Budget>() {
                @Override
                protected void updateItem(Budget budget, boolean empty) {
                    super.updateItem(budget, empty);
                    if (empty || budget == null) {
                        setStyle("");
                    } else if (budget.isExceeded()) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (budget.getUsagePercentage() > 80) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            });

            budgetTable.setItems(budgetObservableList);

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella budget: " + e.getMessage());
        }
    }

    /**
     * Mostra un messaggio di alert informativo, di errore o conferma.
     *
     * @param type    tipo di alert (INFO, WARNING, ERROR, CONFIRMATION)
     * @param title   titolo della finestra di alert
     * @param message messaggio da mostrare
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
