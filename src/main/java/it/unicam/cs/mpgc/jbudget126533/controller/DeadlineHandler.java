package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private final TableView<ScheduledTransaction> scheduledTable;

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
                           Label dueTodayDeadlinesLabel, Label futureDeadlinesLabel,
                           TableView<ScheduledTransaction> scheduledTable) {
        this.ledger = ledger;
        this.deadlinesTable = deadlinesTable;
        this.deadlineFilterType = deadlineFilterType;
        this.totalDeadlinesLabel = totalDeadlinesLabel;
        this.overdueDeadlinesLabel = overdueDeadlinesLabel;
        this.dueTodayDeadlinesLabel = dueTodayDeadlinesLabel;
        this.futureDeadlinesLabel = futureDeadlinesLabel;
        this.scheduledTable = scheduledTable;
        IFileManagement fileManagement = new FileManagement();

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
        deadlineFilterType.getItems().addFirst(null);
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
        showAlert("Aggiornamento", "Scadenze aggiornate!");
    }

    /**
     * Processa la scadenza selezionata segnandola come pagata.
     *
     * @param event evento di azione
     */
    public void processSelectedDeadline(ActionEvent event) {
        Deadline selected = deadlinesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // 1. Per le rate di ammortamento, marca la rata come pagata
                if (selected.getDeadlineType() == DeadlineType.AMORTIZATION_INSTALLMENT) {
                    processAmortizationDeadline(selected);
                }
                // 2. Per le transazioni programmate, esegui la transazione
                else if (selected.getDeadlineType() == DeadlineType.SCHEDULED_TRANSACTION) {
                    processScheduledTransactionDeadline(selected);
                }

                // 3. Aggiorna l'interfaccia
                deadlinesTable.refresh();
                updateDeadlineCounters();

                showAlert("Successo", "Scadenza processata!");

            } catch (Exception e) {
                showAlert("Errore", "Impossibile processare la scadenza: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Processa una scadenza di ammortamento (marca la rata come pagata).
     */
    private void processAmortizationDeadline(Deadline deadline) {
        try {
            // Trova la rata corrispondente nel piano di ammortamento
            String sourceId = deadline.getSourceId();
            if (sourceId != null && sourceId.contains("_")) {
                String[] parts = sourceId.split("_");
                String planId = parts[0];
                int installmentNumber = Integer.parseInt(parts[1]);

                // Trova il piano di ammortamento
                Optional<AmortizationPlan> planOptional = ledger.getAmortizationPlans().stream()
                        .filter(p -> p.getId().equals(planId))
                        .findFirst();

                if (planOptional.isPresent()) {
                    AmortizationPlan plan = planOptional.get();

                    // Trova la rata e marcala come pagata
                    for (Installment installment : plan.getInstallments()) {
                        if (installment.getNumber() == installmentNumber) {
                            installment.setPaid(true);

                            // Crea la transazione corrispondente
                            createTransactionFromAmortization(installment, plan);

                            // Salva i cambiamenti nei piani di ammortamento
                            ledger.saveAmortizationPlans();

                            System.out.println("Rata " + installmentNumber + " del piano " + plan.getDescription() + " marcata come pagata");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel processamento rata ammortamento: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Crea una transazione da una rata di ammortamento.
     */
    private void createTransactionFromAmortization(Installment installment, AmortizationPlan plan) {
        try {
            Person systemPerson = new Person("Ammortamento");

            ITransaction transaction = new Transaction(
                    MovementType.SPESA,
                    systemPerson,
                    -installment.getTotalAmount(), // Importo negativo (spesa)
                    LocalDate.now(),
                    plan.getTags()
            );

            ledger.addTransaction(transaction);
            ledger.write(transaction);

            System.out.println("Transazione creata: " + transaction.getUser() + " - " + transaction.getMoney() + "€");

        } catch (Exception e) {
            System.err.println("Errore nella creazione transazione ammortamento: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Processa una scadenza di transazione programmata.
     */
    private void processScheduledTransactionDeadline(Deadline deadline) {
        try {
            List<ScheduledTransaction> scheduledTransactions = ledger.getScheduledTransactions();
            boolean found = false;

            for (ScheduledTransaction st : scheduledTransactions) {
                if (isMatchingScheduledTransaction(st, deadline)) {

                    // Esegui la transazione programmata
                    ITransaction transaction = st.execute();
                    if (transaction != null) {
                        ledger.addTransaction(transaction);
                        ledger.write(transaction);
                        found = true;

                        System.out.println("✅ Transazione programmata eseguita: " + transaction.getUser() +
                                " - Importo: " + transaction.getMoney() + "€");

                        // SALVA le modifiche (data aggiornata) nel file JSON
                        ledger.getScheduledTransactionManager().saveScheduledTransactions();

                        // Marca la scadenza come pagata
                        deadline.setPaid(true);

                        // AGGIORNA TUTTE LE TABELLE
                        updateAllTables();
                    }
                    break;
                }
            }

            if (!found) {
                System.out.println("⚠️ Transazione programmata non trovata: " + deadline.getDescription());
            }

        } catch (Exception e) {
            System.err.println("❌ Errore nel processamento transazione programmata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Aggiorna tutte le tabelle correlate
     */
    private void updateAllTables() {
        // 1. Aggiorna la tabella delle scadenze
        deadlinesTable.refresh();

        // 2. Aggiorna la tabella delle transazioni programmate
        if (scheduledTable != null) {
            scheduledTable.refresh();
        }

        // 3. Aggiorna i contatori delle scadenze
        updateDeadlineCounters();

        System.out.println("🔄 Tutte le tabelle aggiornate");
    }

    /**
     * Verifica se la transazione programmata corrisponde alla scadenza
     */
    private boolean isMatchingScheduledTransaction(ScheduledTransaction st, Deadline deadline) {
        return st.isActive() &&
                st.getDescription().equals(deadline.getDescription()) &&
                st.getNextExecutionDate().equals(deadline.getDueDate()) &&
                st.getType() == deadline.getType() &&
                Math.abs(st.getAmount() - Math.abs(deadline.getAmount())) < 0.01;
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
            if (!deadlinesTable.getColumns().isEmpty()) {
                TableColumn<Deadline, String> descColumn = (TableColumn<Deadline, String>)
                        deadlinesTable.getColumns().getFirst();
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

            deadlinesTable.setRowFactory(tv -> new TableRow<>() {
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
     * @param title   titolo della finestra di alert
     * @param message messaggio da mostrare
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
