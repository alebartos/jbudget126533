package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.controller.*;
import it.unicam.cs.mpgc.jbudget126533.model.*;

import java.io.File;
import java.util.Map;

import it.unicam.cs.mpgc.jbudget126533.sync.ConflictResolutionStrategy;
import it.unicam.cs.mpgc.jbudget126533.sync.SyncManager;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller principale per l'interfaccia grafica di JBudget.
 * <p>
 * Questa classe gestisce tutte le interazioni tra l'interfaccia utente e il modello
 * dell'applicazione, coordinando i vari gestori specializzati per le diverse funzionalità.
 * Implementa l'interfaccia Initializable di JavaFX per l'inizializzazione dei componenti UI.
 *
 * <p>Il controller è organizzato in diversi gestori specializzati:
 * <ul>
 *   <li>{@link TransactionHandler} - Gestione transazioni</li>
 *   <li>{@link TagHandler} - Gestione tag e categorie</li>
 *   <li>{@link BudgetHandler} - Gestione budget</li>
 *   <li>{@link ScheduledTransactionHandler} - Gestione transazioni programmate</li>
 *   <li>{@link AmortizationHandler} - Gestione piani di ammortamento</li>
 *   <li>{@link DeadlineHandler} - Gestione scadenze</li>
 *   <li>{@link StatisticsHandler} - Gestione statistiche e report</li>
 * </ul>
 */
public class GUIController implements Initializable {
    /**
     * Costruttore del controller principale.
     * Inizializza il ledger e carica tutti i tag dal sistema di persistenza.
     */
    private TransactionHandler transactionHandler;
    private TagHandler tagHandler;
    private BudgetHandler budgetHandler;
    private ScheduledTransactionHandler scheduledTransactionHandler;
    private AmortizationHandler amortizationHandler;
    private DeadlineHandler deadlineHandler;
    private StatisticsHandler statisticsHandler;
    private final SyncManager syncManager = new SyncManager();

    // Riferimento al TabPane principale
    @FXML private TabPane tabPane;

    // --- COMPONENTI DASHBOARD ---
    @FXML private Label balance;
    @FXML private ImageView image;

    // --- COMPONENTI TRANSAZIONI ---
    @FXML private ChoiceBox<MovementType> typeTransaction;
    @FXML private TextField userTransaction;
    @FXML private Button deleteTransactionButton;
    @FXML private TextField moneyTransaction;
    @FXML private DatePicker dateTransaction;
    @FXML private TableView<ITransaction> transactionTable;
    @FXML private ListView<ITag> transactionTagsListView;

    // --- COMPONENTI TAG MULTIPLI ---
    @FXML private ListView<ITag> availableTagsListView;
    @FXML private ListView<ITag> selectedTagsListView;
    @FXML private TextField newTagTextField;
    @FXML private TreeView<ITag> tagHierarchyTreeView;

    // --- COMPONENTI STATISTICHE ---
    @FXML private Label balanceForRange;
    @FXML private DatePicker dateStartForRange;
    @FXML private DatePicker dateEndForRange;
    @FXML private ChoiceBox<MovementType> choiceForRange;
    @FXML private Label balanceTrend;
    @FXML private DatePicker dateStartForTrend;
    @FXML private DatePicker dateEndForTrend;
    @FXML private TableView<Map.Entry<String, Double>> tagTable;
    @FXML private ChoiceBox<MovementType> choiceTypeForEachTag;

    // --- COMPONENTI BUDGET ---
    @FXML private ListView<ITag> budgetTagsListView;
    @FXML private TextField budgetAmountField;
    @FXML private DatePicker budgetStartDate;
    @FXML private DatePicker budgetEndDate;
    @FXML private TableView<Budget> budgetTable;

    // --- COMPONENTI TRANSAZIONI PROGRAMMATE ---
    @FXML private TextField scheduledDescField;
    @FXML private TextField scheduledAmountField;
    @FXML private ChoiceBox<MovementType> scheduledType;
    @FXML private ListView<ITag> scheduledTagsListView;
    @FXML private ChoiceBox<RecurrenceType> scheduledRecurrence;
    @FXML private DatePicker scheduledStartDate;
    @FXML private DatePicker scheduledEndDate;
    @FXML private TableView<ScheduledTransaction> scheduledTable;

    // Componenti per piano di ammortamento
    @FXML private TextField amortDescription;
    @FXML private TextField amortPrincipal;
    @FXML private TextField amortInterestRate;
    @FXML private TextField amortInstallments;
    @FXML private DatePicker amortStartDate;
    @FXML private ListView<ITag> amortTags;
    @FXML private TableView<Installment> amortTable;
    @FXML private TableView<AmortizationPlan> amortizationPlansTable;

    // Componenti per lo scadenzario
    @FXML private Tab deadlinesTab;
    @FXML private TableView<Deadline> deadlinesTable;
    @FXML private ChoiceBox<DeadlineType> deadlineFilterType;
    @FXML private Label totalDeadlinesLabel;
    @FXML private Label overdueDeadlinesLabel;
    @FXML private Label dueTodayDeadlinesLabel;
    @FXML private Label futureDeadlinesLabel;

    //Componenti Persone
    @FXML private Tab personTab;
    @FXML private TableView<Person> personTable;
    @FXML private TextField personNameField;
    @FXML private TextField personEmailField;
    @FXML private TextField personPhoneField;
    @FXML private ListView<ITag> personTagsListView;
    @FXML private ChoiceBox<Person> personChoiceBox;

    private final Ledger ledger;
    private PersonHandler personHandler;

    /**
     * Costruttore del controller principale.
     * Inizializza il ledger e carica tutti i tag dal sistema di persistenza.
     */
    public GUIController() {
        TagManager.loadAllTags();
        ledger = new Ledger(new TransactionManager());
    }

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     * Configura tutti i componenti dell'interfaccia e inizializza i gestori specializzati.
     *
     * @param location La location utilizzata per risolvere i percorsi relativi per l'oggetto root
     * @param resources Le risorse utilizzate per localizzare l'oggetto root
     *
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ledger.read();

            // Inizializza i gestori
            transactionHandler = new TransactionHandler(ledger, typeTransaction, personChoiceBox,
                    moneyTransaction, dateTransaction, transactionTable, transactionTagsListView, balance);

            tagHandler = new TagHandler(availableTagsListView, selectedTagsListView,
                    newTagTextField, tagHierarchyTreeView, transactionTagsListView,
                    budgetTagsListView, scheduledTagsListView, amortTags);

            budgetHandler = new BudgetHandler(ledger, budgetTagsListView, budgetAmountField,
                    budgetStartDate, budgetEndDate, budgetTable);

            scheduledTransactionHandler = new ScheduledTransactionHandler(ledger, scheduledDescField,
                    scheduledAmountField, scheduledType, scheduledTagsListView, scheduledRecurrence,
                    scheduledStartDate, scheduledEndDate, scheduledTable);

            amortizationHandler = new AmortizationHandler(ledger, amortDescription, amortPrincipal,
                    amortInterestRate, amortInstallments, amortStartDate, amortTags,
                    amortTable, amortizationPlansTable);

            deadlineHandler = new DeadlineHandler(ledger, deadlinesTable, deadlineFilterType,
                    totalDeadlinesLabel, overdueDeadlinesLabel, dueTodayDeadlinesLabel,
                    futureDeadlinesLabel, scheduledTable);

            statisticsHandler = new StatisticsHandler(ledger, balanceForRange, dateStartForRange,
                    dateEndForRange, choiceForRange, balanceTrend, dateStartForTrend,
                    dateEndForTrend, tagTable, choiceTypeForEachTag);

            personHandler = new PersonHandler(personTable, personNameField, personEmailField, personPhoneField);


            // Configura componenti UI
            initializeUIComponents();

            // Carica dati
            personHandler.initializePersonManagement();
            loadInitialData();
            ledger.updateBudgets();

            // Carica immagine
            loadImage();

            personHandler.setOnPersonListChanged(() -> {
                // Aggiorna la ChoiceBox nelle transazioni
                if (transactionHandler != null) {
                    transactionHandler.refreshPersonList();
                }
            });

        } catch (Exception e) {
            System.err.println("Errore grave nell'inizializzazione: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Errore Critico",
                    "Impossibile avviare l'applicazione. Controlla i file di dati.");
        }
    }

    /**
     * Inizializza i componenti dell'interfaccia utente con valori predefiniti.
     * Configura le date predefinite per i vari selettori di data e inizializza le choicebox.
     */
    private void initializeUIComponents() {
        // Configura valori di default
        dateTransaction.setValue(LocalDate.now());
        dateStartForRange.setValue(LocalDate.now().minusMonths(1));
        dateEndForRange.setValue(LocalDate.now());
        dateStartForTrend.setValue(LocalDate.now().minusMonths(3));
        dateEndForTrend.setValue(LocalDate.now());
        budgetStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        budgetEndDate.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        scheduledStartDate.setValue(LocalDate.now());

        // Inizializza choicebox
        transactionHandler.initializeChoiceBoxes();
        statisticsHandler.initializeChoiceBoxes();
        scheduledTransactionHandler.initializeChoiceBoxes();
    }

    /**
     * Carica i dati iniziali nell'interfaccia utente.
     * Aggiorna il bilancio, carica le transazioni, inizializza la gestione dei tag,
     * aggiorna le tabelle dei budget, transazioni programmate, piani di ammortamento e scadenze.
     */
    private void loadInitialData() {
        transactionHandler.updateBalance();
        transactionHandler.loadTransactionTable();
        tagHandler.initializeTagManagement();
        budgetHandler.updateBudgetTable();
        scheduledTransactionHandler.updateScheduledTable();
        amortizationHandler.loadAmortizationPlans();
        deadlineHandler.loadDeadlines();
    }

    /**
     * Carica l'immagine dell'applicazione dall'resources folder.
     * Gestisce silenziosamente eventuali errori di caricamento.
     */
    private void loadImage() {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("jbudget.jpg")));
            image.setImage(img);
        } catch (Exception e) {
            System.out.println();
        }
    }

    // ==================== METODI DI NAVIGAZIONE ====================

    /**
     * Mostra la scheda per aggiungere una nuova transazione.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showAddTransaction(ActionEvent event) {
        tabPane.getSelectionModel().select(1);
    }

    /**
     * Mostra la scheda di gestione dei budget.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showBudget(ActionEvent event) {
        tabPane.getSelectionModel().select(4);
    }

    /**
     * Mostra la scheda delle transazioni programmate.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showTransazioniProgrammate(ActionEvent event) {
        tabPane.getSelectionModel().select(5);
    }

    /**
     * Mostra la scheda delle statistiche e report.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showStatistics(ActionEvent event) {
        tabPane.getSelectionModel().select(3);
    }

    /**
     * Mostra la scheda di gestione dei tag e reinizializza la gestione dei tag.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showTagManager(ActionEvent event) {
        tabPane.getSelectionModel().select(2);
        tagHandler.initializeTagManagement();
    }

    /**
     * Mostra la scheda delle impostazioni (funzionalità in sviluppo).
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Impostazioni", "Funzionalità in sviluppo!");
    }

    /**
     * Mostra la scheda di tutte le transazioni e aggiorna la tabella delle transazioni.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showAllTransactions(ActionEvent event) {
        tabPane.getSelectionModel().select(1);
        transactionHandler.loadTransactionTable();
    }

    /**
     * Aggiorna le liste dei tag disponibili e mostra un messaggio di conferma.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void refreshTagLists(ActionEvent event) {
        tagHandler.initializeTagManagement();
        tagHandler.updateAvailableTransactionTags();
        showAlert(Alert.AlertType.INFORMATION, "Aggiornamento", "Liste tag aggiornate!");
    }

    // ==================== DELEGA AI GESTORI SPECIALIZZATI ====================

    /**
     * Delega l'aggiunta/rimozione di una nuova transazione al TransactionHandler.
     *
     * @param actionEvent L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void addTransaction(ActionEvent actionEvent) {
        transactionHandler.addTransaction(actionEvent);
    }

    @FXML
    public void deleteSelectedTransaction(ActionEvent event) {
        transactionHandler.deleteSelectedTransaction();
    }

    /**
     * Delega l'aggiornamento del bilancio al TransactionHandler.
     *
     * @param actionEvent L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void updateBalance(ActionEvent actionEvent) {
        transactionHandler.updateBalance();
    }

    /**
     * Delega la creazione di un nuovo tag al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void createNewTag(ActionEvent event) {
        tagHandler.createNewTag(event);
    }

    /**
     * Delega la rimozione del tag selezionato al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void removeSelectedTag(ActionEvent event) {
        tagHandler.removeSelectedTag(event);
    }

    /**
     * Delega l'aggiunta del tag selezionato al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void addSelectedTag(ActionEvent event) {
        tagHandler.addSelectedTag(event);
    }

    /**
     * Delega l'eliminazione del tag selezionato al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void deleteSelectedTag(ActionEvent event) {
        tagHandler.deleteSelectedTag(event);
    }

    /**
     * Delega l'aggiornamento di tutti i tag al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void refreshAllTags(ActionEvent event) {
        tagHandler.refreshAllTags(event);
    }

    /**
     * Delega l'esportazione dei tag al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void exportTags(ActionEvent event) {
        tagHandler.exportTags(event);
    }

    /**
     * Delega l'importazione dei tag al TagHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void importTags(ActionEvent event) {
        tagHandler.importTags(event);
    }

    /**
     * Delega l'aggiornamento del bilancio per un intervallo di date allo StatisticsHandler.
     *
     * @param actionEvent L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void updateBalanceForRange(ActionEvent actionEvent) {
        statisticsHandler.updateBalanceForRange(actionEvent);
    }

    /**
     * Delega l'aggiornamento dell'andamento del bilancio allo StatisticsHandler.
     *
     * @param actionEvent L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void updateBalanceTrend(ActionEvent actionEvent) {
        statisticsHandler.updateBalanceTrend(actionEvent);
    }

    /**
     * Delega la visualizzazione della tabella dei tag per tipo allo StatisticsHandler.
     *
     * @param actionEvent L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showTypeTagTable(ActionEvent actionEvent) {
        statisticsHandler.showTypeTagTable(actionEvent);
    }

    /**
     * Delega l'impostazione di un nuovo budget al BudgetHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void setBudget(ActionEvent event) {
        budgetHandler.setBudget(event);
    }

    /**
     * Delega l'aggiornamento dei budget al BudgetHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void updateBudgets(ActionEvent event) {
        budgetHandler.updateBudgets(event);
    }

    /**
     * Delega la rimozione di un budget selezionato al BudgetHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void removeBudget(ActionEvent event) {
        budgetHandler.removeBudget(event);
    }

    /**
     * Delega l'aggiunta di una transazione programmata al ScheduledTransactionHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void addScheduledTransaction(ActionEvent event) {
        scheduledTransactionHandler.addScheduledTransaction(event);
    }

    /**
     * Delega la rimozione di una transazione programmata selezionata al ScheduledTransactionHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void removeScheduledTransaction(ActionEvent event) {
        scheduledTransactionHandler.removeScheduledTransaction(event);
    }

    /**
     * Delega il controllo delle transazioni programmate scadute al ScheduledTransactionHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void checkScheduledTransactions(ActionEvent event) {
        scheduledTransactionHandler.checkScheduledTransactions(event);
    }

    /**
     * Delega la creazione di un nuovo piano di ammortamento all'AmortizationHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void createAmortizationPlan(ActionEvent event) {
        amortizationHandler.createAmortizationPlan(event);
    }

    /**
     * Delega l'aggiornamento della tabella degli ammortamenti all'AmortizationHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void refreshAmortizationTable(ActionEvent event) {
        amortizationHandler.refreshAmortizationTable(event);
    }

    /**
     * Delega l'eliminazione del piano di ammortamento selezionato all'AmortizationHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void deleteSelectedAmortizationPlan(ActionEvent event) {
        amortizationHandler.deleteSelectedAmortizationPlan(event);
    }

    /**
     * Delega l'elaborazione delle rate scadute all'AmortizationHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void processDueInstallments(ActionEvent event) {
        amortizationHandler.processDueInstallments(event);
    }

    /**
     * Delega l'aggiornamento delle scadenze al DeadlineHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void refreshDeadlines(ActionEvent event) {
        deadlineHandler.refreshDeadlines(event);
    }

    /**
     * Delega l'elaborazione della scadenza selezionata al DeadlineHandler.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void processSelectedDeadline(ActionEvent event) {
        deadlineHandler.processSelectedDeadline(event);
    }

    /**
     * Mostra la scheda delle scadenze e carica i dati delle scadenze.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showDeadlines(ActionEvent event) {
        tabPane.getSelectionModel().select(deadlinesTab);
        deadlineHandler.loadDeadlines();
    }

    /**
     * Mostra la scheda dei piani di ammortamento e aggiorna la tabella delle transazioni.
     *
     * @param event L'evento di azione che ha triggerato questo metodo
     */
    @FXML
    public void showAmortization(ActionEvent event) {
        tabPane.getSelectionModel().select(6);
        transactionHandler.loadTransactionTable();
    }

    // ==================== METODI PERSON ====================
    @FXML
    public void addPerson(ActionEvent event) {
        personHandler.addPerson(event);
    }


    @FXML
    public void showPersonManager(ActionEvent event) {
        tabPane.getSelectionModel().select(8);
        personHandler.initializePersonManagement();
    }


    // ==================== METODI SYNC ====================
    @FXML
    public void exportSyncPackage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Esporta pacchetto di sincronizzazione");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File di sincronizzazione", "*.jbsync")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            boolean success = syncManager.exportSyncPackage(file.getAbsolutePath());
            showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                    "Esportazione",
                    success ? "Pacchetto esportato con successo" : "Errore nell'esportazione");
        }
    }

    @FXML
    public void importSyncPackage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importa pacchetto di sincronizzazione");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("File di sincronizzazione", "*.jbsync")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Chiedi la strategia di risoluzione conflitti
            ChoiceDialog<ConflictResolutionStrategy> dialog = new ChoiceDialog<>(
                    ConflictResolutionStrategy.REMOTE_WINS,
                    ConflictResolutionStrategy.values()
            );
            dialog.setTitle("Risoluzione conflitti");
            dialog.setHeaderText("Scegli come gestire i conflitti");

            Optional<ConflictResolutionStrategy> result = dialog.showAndWait();
            result.ifPresent(strategy -> {
                boolean success = syncManager.importSyncPackage(file.getAbsolutePath(), strategy);
                showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        "Importazione",
                        success ? "Pacchetto importato con successo" : "Errore nell'importazione");
            });
        }
    }
    // ==================== METODI UTILITY ====================

    /**
     * Mostra un alert all'utente con il tipo, titolo e messaggio specificati.
     *
     * @param type Il tipo di alert (ERROR, WARNING, INFORMATION, ecc.)
     * @param title Il titolo dell'alert
     * @param message Il messaggio da visualizzare
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Aggiorna tutti i display dell'interfaccia utente.
     * Aggiorna il bilancio, la tabella delle transazioni e la tabella dei budget.
     */
    public void updateAllDisplays() {
        transactionHandler.updateBalance();
        transactionHandler.loadTransactionTable();
        budgetHandler.updateBudgetTable();
    }
}
