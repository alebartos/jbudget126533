package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Gestisce le scadenze (Deadline) nell'applicazione.
 * Fornisce funzionalità per:
 * <ul>
 *     <li>Caricare le scadenze dal Ledger</li>
 *     <li>Filtrare le scadenze per tipo</li>
 *     <li>Processare le scadenze selezionate</li>
 *     <li>Aggiornare i contatori di scadenze totali, scadute, odierne e future</li>
 *     <li>Visualizzare le scadenze in una tabella con formattazione e colorazione condizionale</li>
 * </ul>
 */
public class DeadlineHandler {

    private final Ledger ledger;
    private final TableView<Deadline> deadlinesTable;
    private final ChoiceBox<DeadlineType> deadlineFilterType;
    private final Label totalDeadlinesLabel;
    private final Label overdueDeadlinesLabel;
    private final Label dueTodayDeadlinesLabel;
    private final Label futureDeadlinesLabel;

    private final ObservableList<Deadline> deadlinesObservableList = FXCollections.observableArrayList();
    private final FilteredList<Deadline> filteredDeadlines;

    /**
     * Costruttore della classe.
     *
     * @param ledger                il registro principale che gestisce le scadenze
     * @param deadlinesTable        tabella delle scadenze
     * @param deadlineFilterType    scelta del tipo di scadenza da filtrare
     * @param totalDeadlinesLabel   etichetta che mostra il totale delle scadenze
     * @param overdueDeadlinesLabel etichetta che mostra le scadenze scadute
     * @param dueTodayDeadlinesLabel etichetta che mostra le scadenze odierne
     * @param futureDeadlinesLabel  etichetta che mostra le scadenze future
     */
    public DeadlineHandler(Ledger ledger, TableView<Deadline> deadlinesTable,
                           ChoiceBox<DeadlineType> deadlineFilterType,
                           Label totalDeadlinesLabel, Label overdueDeadlinesLabel,
                           Label dueTodayDeadlinesLabel, Label futureDeadlinesLabel) {
        this.ledger = ledger;
        this.deadlinesTable = deadlinesTable;
        this.deadlineFilterType = deadlineFilterType;
        this.totalDeadlinesLabel = totalDeadlinesLabel;
        this.overdueDeadlinesLabel = overdueDeadlinesLabel;
        this.dueTodayDeadlinesLabel = dueTodayDeadlinesLabel;
        this.futureDeadlinesLabel = futureDeadlinesLabel;

        this.filteredDeadlines = new FilteredList<>(deadlinesObservableList);
        configureDeadlinesTable();
        initializeFilter();
    }

    /**
     * Inizializza il filtro delle scadenze per tipo e imposta il listener
     * per aggiornare la lista filtrata quando cambia la selezione.
     */
    private void initializeFilter() {
        deadlineFilterType.getItems().addAll(DeadlineType.values());
        deadlineFilterType.getItems().add(0, null);
        deadlineFilterType.setValue(null);

        deadlineFilterType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                filteredDeadlines.setPredicate(deadline -> true);
            } else {
                filteredDeadlines.setPredicate(deadline -> deadline.getDeadlineType() == newVal);
            }
        });
    }

    /**
     * Carica tutte le scadenze dal ledger e aggiorna la tabella e i contatori.
     */
    public void loadDeadlines() {
        if (deadlinesTable != null && ledger != null) {
            deadlinesObservableList.clear();
            deadlinesObservableList.addAll(ledger.getAllDeadlines());
            updateDeadlineCounters();
        }
    }

    /**
     * Aggiorna le scadenze richiamando {@link #loadDeadlines()} e mostra un messaggio informativo.
     *
     * @param event evento di azione
     */
    public void refreshDeadlines(ActionEvent event) {
        loadDeadlines();
        showAlert(Alert.AlertType.INFORMATION, "Aggiornamento", "Scadenze aggiornate!");
    }

    /**
     * Processa la scadenza selezionata segnandola come pagata.
     *
     * @param event evento di azione
     */
    public void processSelectedDeadline(ActionEvent event) {
        Deadline selected = deadlinesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setPaid(true);
            deadlinesTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Successo", "Scadenza processata!");
        }
    }

    /**
     * Aggiorna i contatori delle scadenze totali, scadute, odierne e future.
     */
    private void updateDeadlineCounters() {
        LocalDate today = LocalDate.now();

        long total = deadlinesObservableList.size();
        long overdue = 0;
        long dueToday = 0;
        long future = 0;

        for (Deadline d : deadlinesObservableList) {
            if (d.isPaid()) continue; // ignora le scadenze già pagate

            if (d.getDueDate().isBefore(today)) {
                overdue++;
            } else if (d.getDueDate().equals(today)) {
                dueToday++;
            } else {
                future++;
            }
        }

        if (totalDeadlinesLabel != null)
            totalDeadlinesLabel.setText("Totale Scadenze: " + total);
        if (overdueDeadlinesLabel != null)
            overdueDeadlinesLabel.setText("Scadute: " + overdue);
        if (dueTodayDeadlinesLabel != null)
            dueTodayDeadlinesLabel.setText("Oggi: " + dueToday);
        if (futureDeadlinesLabel != null)
            futureDeadlinesLabel.setText("Future: " + future);
    }


    /**
     * Configura le colonne della tabella delle scadenze e la colorazione condizionale delle righe.
     */
    private void configureDeadlinesTable() {
        try {
            if (deadlinesTable.getColumns().size() > 0) {
                TableColumn<Deadline, String> descColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().get(0);
                descColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDescription()));
            }

            if (deadlinesTable.getColumns().size() > 1) {
                TableColumn<Deadline, String> amountColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().get(1);
                amountColumn.setCellValueFactory(cellData -> {
                    double amount = cellData.getValue().getAmount();
                    String sign = cellData.getValue().getType() == MovementType.SPESA ? "-" : "+";
                    return new SimpleStringProperty(String.format("%s%,.2f €", sign, Math.abs(amount)));
                });
            }

            if (deadlinesTable.getColumns().size() > 2) {
                TableColumn<Deadline, String> typeColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().get(2);
                typeColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDeadlineType().toString()));
            }

            if (deadlinesTable.getColumns().size() > 3) {
                TableColumn<Deadline, String> dateColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().get(3);
                dateColumn.setCellValueFactory(cellData ->
                        new SimpleStringProperty(cellData.getValue().getDueDate().toString()));
            }

            if (deadlinesTable.getColumns().size() > 4) {
                TableColumn<Deadline, String> statusColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().get(4);
                statusColumn.setCellValueFactory(cellData -> {
                    Deadline deadline = cellData.getValue();
                    String status = deadline.isPaid() ? "✅ Pagato" :
                            deadline.isOverdue() ? "⚠️ Scaduto" :
                                    deadline.isDueToday() ? "📅 Oggi" : "⏳ Futuro";
                    return new SimpleStringProperty(status);
                });
            }

            deadlinesTable.setRowFactory(tv -> new TableRow<Deadline>() {
                @Override
                protected void updateItem(Deadline deadline, boolean empty) {
                    super.updateItem(deadline, empty);
                    if (empty || deadline == null) {
                        setStyle("");
                    } else if (deadline.isOverdue()) {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    } else if (deadline.isDueToday()) {
                        setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #ff8f00;");
                    } else if (deadline.isPaid()) {
                        setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                    } else {
                        setStyle("");
                    }
                }
            });

            deadlinesTable.setItems(filteredDeadlines);

        } catch (Exception e) {
            System.err.println("Errore nella configurazione della tabella deadlines: " + e.getMessage());
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
