package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestisce le operazioni relative alla creazione, gestione e visualizzazione dei
 * piani di ammortamento. Fornisce funzionalità per:
 * <ul>
 *   <li>Creare nuovi piani di ammortamento</li>
 *   <li>Caricare e visualizzare i piani esistenti</li>
 *   <li>Eliminare piani selezionati</li>
 *   <li>Processare le rate scadute</li>
 *   <li>Mostrare i dettagli delle singole rate</li>
 * </ul>
 * Questa classe è strettamente legata all'interfaccia JavaFX.
 */
public class AmortizationHandler {

    private final Ledger ledger;
    private final TextField amortDescription;
    private final TextField amortPrincipal;
    private final TextField amortInterestRate;
    private final TextField amortInstallments;
    private final DatePicker amortStartDate;
    private final ListView<ITag> amortTags;
    private final TableView<Installment> amortTable;
    private final TableView<AmortizationPlan> amortizationPlansTable;

    private final ObservableList<AmortizationPlan> amortizationPlansObservableList = FXCollections.observableArrayList();

    /**
     * Costruttore della classe.
     *
     * @param ledger                 il registro principale che gestisce i dati
     * @param amortDescription       campo descrizione del piano
     * @param amortPrincipal         campo capitale da ammortizzare
     * @param amortInterestRate      campo tasso d’interesse
     * @param amortInstallments      campo numero rate
     * @param amortStartDate         campo data di inizio
     * @param amortTags              lista dei tag selezionabili
     * @param amortTable             tabella delle rate
     * @param amortizationPlansTable tabella dei piani di ammortamento
     */
    public AmortizationHandler(Ledger ledger, TextField amortDescription, TextField amortPrincipal,
                               TextField amortInterestRate, TextField amortInstallments,
                               DatePicker amortStartDate, ListView<ITag> amortTags,
                               TableView<Installment> amortTable, TableView<AmortizationPlan> amortizationPlansTable) {
        this.ledger = ledger;
        this.amortDescription = amortDescription;
        this.amortPrincipal = amortPrincipal;
        this.amortInterestRate = amortInterestRate;
        this.amortInstallments = amortInstallments;
        this.amortStartDate = amortStartDate;
        this.amortTags = amortTags;
        this.amortTable = amortTable;
        this.amortizationPlansTable = amortizationPlansTable;

        configureAmortizationTables();
        setupSelectionListener();
    }

    /**
     * Configura un listener per aggiornare la tabella delle rate quando
     * viene selezionato un piano di ammortamento.
     */
    private void setupSelectionListener() {
        if (amortizationPlansTable != null) {
            amortizationPlansTable.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            refreshAmortizationTable(null);
                        }
                    }
            );
        }
    }

    /**
     * Crea un nuovo piano di ammortamento a partire dai dati inseriti
     * nei campi dell’interfaccia e lo salva nel ledger.
     *
     * @param event evento di azione (es. click su bottone)
     * @throws NumberFormatException se i campi numerici non sono validi
     * @throws Exception             per errori generici durante la creazione
     */
    public void createAmortizationPlan(ActionEvent event) {
        try {
            String description = amortDescription.getText().trim();
            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci una descrizione!");
                return;
            }

            double principal = Double.parseDouble(amortPrincipal.getText());
            double interestRate = Double.parseDouble(amortInterestRate.getText());
            int installments = Integer.parseInt(amortInstallments.getText());
            LocalDate startDate = amortStartDate.getValue();

            List<ITag> selectedTags = new ArrayList<>(amortTags.getSelectionModel().getSelectedItems());
            if (selectedTags.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona almeno un tag!");
                return;
            }

            if (startDate == null) {
                showAlert(Alert.AlertType.ERROR, "Errore", "Seleziona una data di inizio!");
                return;
            }

            AmortizationPlan plan = ledger.createAmortizationPlan(description, principal, interestRate, installments, startDate, selectedTags);
            ledger.saveAmortizationPlans();
            loadAmortizationPlans();
            clearAmortizationFields();

            showAlert(Alert.AlertType.INFORMATION, "Successo", "Piano di ammortamento creato e salvato!");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Dati numerici non validi!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna la tabella delle rate mostrando quelle relative al piano selezionato.
     * Se non è selezionato alcun piano, la tabella viene svuotata.
     *
     * @param event evento di azione (può essere null se chiamato internamente)
     */
    public void refreshAmortizationTable(ActionEvent event) {
        AmortizationPlan selectedPlan = amortizationPlansTable.getSelectionModel().getSelectedItem();
        if (selectedPlan != null) {
            showAmortizationDetails(selectedPlan);
        } else {
            amortTable.getItems().clear();
        }
    }

    /**
     * Elimina il piano di ammortamento attualmente selezionato previa conferma
     * da parte dell'utente.
     *
     * @param event evento di azione (es. click su bottone)
     */
    public void deleteSelectedAmortizationPlan(ActionEvent event) {
        AmortizationPlan selected = amortizationPlansTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Conferma eliminazione");
            confirmation.setHeaderText("Eliminare il piano di ammortamento '" + selected.getDescription() + "'?");
            confirmation.setContentText("Questa operazione non può essere annullata.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = ledger.deleteAmortizationPlan(selected);
                if (success) {
                    loadAmortizationPlans();
                    amortTable.getItems().clear();
                    showAlert(Alert.AlertType.INFORMATION, "Successo", "Piano di ammortamento eliminato!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile eliminare il piano!");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona un piano da eliminare!");
        }
    }

    /**
     * Processa tutte le rate scadute registrandole come dovute
     * e aggiornando la visualizzazione delle tabelle.
     *
     * @param event evento di azione (es. click su bottone)
     */
    public void processDueInstallments(ActionEvent event) {
        ledger.processAmortizationDueDates();
        loadAmortizationPlans();

        AmortizationPlan selectedPlan = amortizationPlansTable.getSelectionModel().getSelectedItem();
        if (selectedPlan != null) {
            showAmortizationDetails(selectedPlan);
        }

        showAlert(Alert.AlertType.INFORMATION, "Scadenze", "Rate scadute processate!");
    }

    /**
     * Carica tutti i piani di ammortamento dal ledger e li mostra nella tabella.
     */
    public void loadAmortizationPlans() {
        if (amortizationPlansTable != null && ledger != null) {
            amortizationPlansObservableList.clear();
            amortizationPlansObservableList.addAll(ledger.getAmortizationPlans());
            amortizationPlansTable.refresh();
        }
    }

    /**
     * Mostra i dettagli delle rate di un determinato piano nella tabella delle rate.
     *
     * @param plan piano di ammortamento selezionato
     */
    private void showAmortizationDetails(AmortizationPlan plan) {
        if (amortTable != null && plan != null) {
            amortTable.setItems(FXCollections.observableArrayList(plan.getInstallments()));
        }
    }

    /**
     * Pulisce tutti i campi di input del form di creazione piano.
     */
    private void clearAmortizationFields() {
        amortDescription.clear();
        amortPrincipal.clear();
        amortInterestRate.clear();
        amortInstallments.clear();
        amortStartDate.setValue(null);
        amortTags.getSelectionModel().clearSelection();
    }

    /**
     * Configura le colonne delle tabelle dei piani di ammortamento e delle rate
     * impostando i valori da mostrare in ciascuna colonna.
     */
    private void configureAmortizationTables() {
        try {
            // Configura tabella piani di ammortamento
            if (amortizationPlansTable.getColumns().size() > 0) {
                TableColumn<AmortizationPlan, String> planDescColumn = (TableColumn<AmortizationPlan, String>)
                        amortizationPlansTable.getColumns().get(0);
                planDescColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDescription()));
            }

            if (amortizationPlansTable.getColumns().size() > 1) {
                TableColumn<AmortizationPlan, String> planPrincipalColumn = (TableColumn<AmortizationPlan, String>)
                        amortizationPlansTable.getColumns().get(1);
                planPrincipalColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%,.2f €", cellData.getValue().getPrincipalAmount())));
            }

            if (amortizationPlansTable.getColumns().size() > 2) {
                TableColumn<AmortizationPlan, String> planRateColumn = (TableColumn<AmortizationPlan, String>)
                        amortizationPlansTable.getColumns().get(2);
                planRateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%.2f %%", cellData.getValue().getAnnualInterestRate())));
            }

            if (amortizationPlansTable.getColumns().size() > 3) {
                TableColumn<AmortizationPlan, String> planInstallmentsColumn = (TableColumn<AmortizationPlan, String>)
                        amortizationPlansTable.getColumns().get(3);
                planInstallmentsColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.valueOf(cellData.getValue().getNumberOfInstallments())));
            }

            if (amortizationPlansTable.getColumns().size() > 4) {
                TableColumn<AmortizationPlan, String> planStartDateColumn = (TableColumn<AmortizationPlan, String>)
                        amortizationPlansTable.getColumns().get(4);
                planStartDateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getStartDate().toString()));
            }

            // Configura tabella rate
            if (amortTable.getColumns().size() > 0) {
                TableColumn<Installment, String> installmentNumberColumn = (TableColumn<Installment, String>)
                        amortTable.getColumns().get(0);
                installmentNumberColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.valueOf(cellData.getValue().getNumber())));
            }

            if (amortTable.getColumns().size() > 1) {
                TableColumn<Installment, String> installmentAmountColumn = (TableColumn<Installment, String>)
                        amortTable.getColumns().get(1);
                installmentAmountColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(String.format("%,.2f €", cellData.getValue().getTotalAmount())));
            }

            if (amortTable.getColumns().size() > 2) {
                TableColumn<Installment, String> installmentDueDateColumn = (TableColumn<Installment, String>)
                        amortTable.getColumns().get(2);
                installmentDueDateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDueDate().toString()));
            }

            if (amortTable.getColumns().size() > 3) {
                TableColumn<Installment, String> installmentPaidColumn = (TableColumn<Installment, String>)
                        amortTable.getColumns().get(3);
                installmentPaidColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().isPaid() ? "Sì" : "No"));
            }

            amortizationPlansTable.setItems(amortizationPlansObservableList);

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella ammortamento: " + e.getMessage());
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
