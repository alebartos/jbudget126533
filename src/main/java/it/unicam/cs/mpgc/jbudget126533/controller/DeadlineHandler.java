package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final IFileManagement fileManagement;

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
        this.fileManagement = new FileManagement();

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
                refreshDeadlines(null);

                showAlert("Successo", "Scadenza processata!");

            } catch (Exception e) {
                showAlert("Errore", "Impossibile processare la scadenza: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Crea una transazione a partire da una scadenza.
     */
    /**
     * Processa una scadenza di ammortamento (marca la rata come pagata).
     */
    /**
     * Processa una scadenza di ammortamento (versione alternativa più robusta).
     */
    private void processAmortizationDeadline(Deadline deadline) {
        try {
            String description = deadline.getDescription();
            System.out.println("Processing: " + description);

            // Cerca il numero della rata nella descrizione (es: "Rata 12 - Mutuo Casa")
            if (description.startsWith("Rata ")) {
                String[] descParts = description.split(" ");
                if (descParts.length >= 2) {
                    try {
                        // Estrai il numero dalla descrizione "Rata X - Descrizione"
                        String numberStr = descParts[1];
                        if (numberStr.contains("-")) {
                            numberStr = numberStr.split("-")[0];
                        }
                        int installmentNumber = Integer.parseInt(numberStr.trim());

                        // Cerca il piano per descrizione
                        for (AmortizationPlan plan : ledger.getAmortizationPlans()) {
                            for (Installment installment : plan.getInstallments()) {
                                if (installment.getNumber() == installmentNumber &&
                                        description.contains(plan.getDescription())) {

                                    installment.setPaid(true);
                                    createTransactionFromAmortization(installment, plan);
                                    ledger.saveAmortizationPlans();

                                    System.out.println("Rata " + installmentNumber + " processata");
                                    return;
                                }
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Impossibile estrarre numero rata da: " + description);
                    }
                }
            }

            System.err.println("Impossibile processare la scadenza: " + description);

        } catch (Exception e) {
            System.err.println("Errore nel processamento rata ammortamento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea una transazione da una rata di ammortamento.
     */
    private void createTransactionFromAmortization(Installment installment, AmortizationPlan plan) {
        try {
            ITransaction transaction = new Transaction(
                    MovementType.SPESA,
                    "Rata " + installment.getNumber() + " - " + plan.getDescription(),
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
            // Trova la transazione programmata corrispondente
            // (implementazione semplificata - dovresti avere un modo per identificarla)
            List<ScheduledTransaction> scheduledTransactions = ledger.getScheduledTransactions();

            for (ScheduledTransaction st : scheduledTransactions) {
                if (st.getDescription().equals(deadline.getDescription()) &&
                        st.getNextExecutionDate().equals(deadline.getDueDate())) {

                    // Esegui la transazione programmata
                    ITransaction transaction = st.execute();
                    if (transaction != null) {
                        ledger.addTransaction(transaction);
                        ledger.write(transaction);
                        System.out.println("Transazione programmata eseguita: " + transaction.getUser());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel processamento transazione programmata: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Ottiene i tag appropriati per una scadenza.
     */
    private List<ITag> getTagsForDeadline(Deadline deadline) {
        List<ITag> tags = new ArrayList<>();

        try {
            // Per rate di ammortamento
            if (deadline.getDeadlineType() == DeadlineType.AMORTIZATION_INSTALLMENT) {
                // Cerca il tag "Mutui" o "Prestiti"
                ITag loanTag = TagManager.getTag("Mutui");
                if (loanTag == null) {
                    loanTag = TagManager.getTag("Prestiti");
                }
                if (loanTag != null) {
                    tags.add(loanTag);
                }
            }
            // Per transazioni programmate
            else if (deadline.getDeadlineType() == DeadlineType.SCHEDULED_TRANSACTION) {
                // Usa tag generici in base al tipo
                if (deadline.getType() == MovementType.SPESA) {
                    ITag expenseTag = TagManager.getTag("Spese Fisse");
                    if (expenseTag != null) tags.add(expenseTag);
                } else {
                    ITag incomeTag = TagManager.getTag("Entrate");
                    if (incomeTag != null) tags.add(incomeTag);
                }
            }

        } catch (Exception e) {
            System.err.println("Errore nell'ottenimento tag: " + e.getMessage());
        }

        return tags;
    }

    /**
     * Salva lo stato aggiornato delle scadenze.
     */
    private void saveDeadlines() {
        try {
            // Ottieni tutte le scadenze
            List<Deadline> allDeadlines = new ArrayList<>(deadlinesObservableList);

            // Salva nel file
            fileManagement.writeObject("deadlines.json", allDeadlines);

            System.out.println("Scadenze salvate: " + allDeadlines.size() + " elementi");

        } catch (Exception e) {
            System.err.println("Errore nel salvataggio scadenze: " + e.getMessage());
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
