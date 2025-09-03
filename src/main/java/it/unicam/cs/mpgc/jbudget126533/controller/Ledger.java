package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import it.unicam.cs.mpgc.jbudget126533.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Classe principale che funge da registro centrale dell'applicazione.
 * Gestisce:
 * <ul>
 *     <li>Transazioni</li>
 *     <li>Budget e gestione dei limiti di spesa</li>
 *     <li>Transazioni programmate</li>
 *     <li>Piani di ammortamento</li>
 *     <li>Scadenze</li>
 * </ul>
 * Collabora con i manager specifici (BudgetManager, ScheduledTransactionManager,
 * AmortizationManager, DeadlineManager) e con l'interfaccia {@link IBudgetManagement}.
 */
public class Ledger {

    private final IFileManagement fileManagement = new FileManagement();
    private final IBudgetManagement budgetManagement;
    private final BudgetManager budgetManager;
    private final ScheduledTransactionManager scheduledTransactionManager;
    private final AmortizationManager amortizationManager;
    private final DeadlineManager deadlineManager;

    /**
     * Costruttore del Ledger.
     * Inizializza i manager e carica i tag.
     *
     * @param budgetManagement l'implementazione dell'interfaccia per la gestione dei budget
     */
    public Ledger(IBudgetManagement budgetManagement) {
        IFileManagement fileManagement = new FileManagement();
        TagManager.setFileManagement(fileManagement);
        TagManager.loadAllTags();
        this.budgetManagement = budgetManagement;
        this.budgetManager = new BudgetManager(budgetManagement, this, fileManagement);
        this.scheduledTransactionManager = new ScheduledTransactionManager(this, fileManagement);
        this.budgetManagement.setScheduledTransactionManager(scheduledTransactionManager);
        this.amortizationManager = new AmortizationManager(fileManagement);
        this.deadlineManager = new DeadlineManager(
                scheduledTransactionManager,
                amortizationManager,
                budgetManager
        );
        if (this.budgetManagement != null) {
            this.budgetManagement.setAmortizationManager(amortizationManager);
        }
    }

    /**
     * Restituisce il bilancio attuale calcolato dal {@link IBudgetManagement}.
     *
     * @return bilancio totale
     */
    public double getBalance() {
        return budgetManagement.getBalance();
    }

    /**
     * Salva i piani di ammortamento correnti.
     */
    public void saveAmortizationPlans() {
        amortizationManager.save();
    }

    /**
     * Aggiunge una nuova transazione e aggiorna i budget.
     *
     * @param transaction transazione da aggiungere
     */
    public void addTransaction(ITransaction transaction) {
        budgetManagement.insert(transaction);
        budgetManager.onNewTransactionAdded(transaction);
        updateBudgets();
    }

    /**
     * Scrive una transazione su file tramite {@link IFileManagement}.
     *
     * @param transaction transazione da scrivere
     */
    public void write(ITransaction transaction) {
        fileManagement.write(transaction);
    }

    /**
     * Legge le transazioni da file e aggiorna la lista nel budgetManagement.
     */
    public void read() {
        try {
            budgetManagement.setList(fileManagement.read());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notifica ai manager che i budget sono cambiati.
     */
    public void notifyBudgetChanged() {
        budgetManager.updateAllBudgets();
    }

    /**
     * Restituisce una funzione che calcola il trend del bilancio tra due date.
     *
     * @param dateStart data di inizio
     * @param dateEnd   data di fine
     * @return funzione che riceve una lista di transazioni e ritorna {@link Pair} con successo e valore
     */
    public Function<ArrayList<ITransaction>, Pair<Boolean, Double>> trendBalance(LocalDate dateStart, LocalDate dateEnd) {
        return budgetManagement.trendBalance(dateStart, dateEnd);
    }

    /**
     * Calcola il bilancio per un tag specifico.
     *
     * @param type tipo di movimento
     * @param tag  nome del tag
     * @return bilancio per il tag
     */
    public double balanceForTag(MovementType type, String tag) {
        return budgetManagement.balanceForTag(type, tag);
    }

    /**
     * Calcola il bilancio tra due date.
     *
     * @param type      tipo di movimento
     * @param dateStart data di inizio
     * @param dateEnd   data di fine
     * @return bilancio per il periodo
     */
    public double balanceForDates(MovementType type, LocalDate dateStart, LocalDate dateEnd) {
        return budgetManagement.balanceForDates(type, dateStart, dateEnd);
    }

    /**
     * Calcola il bilancio per ciascun tag.
     *
     * @param type tipo di movimento
     * @return mappa tag -> valore
     */
    public HashMap<String, Double> balanceForEachTag(MovementType type) {
        return budgetManagement.balanceForEachTag(type);
    }

    /**
     * Restituisce la lista completa delle transazioni.
     *
     * @return lista di transazioni
     */
    public ArrayList<ITransaction> getTransaction() {
        return budgetManagement.getList();
    }

    /**
     * Imposta la lista completa delle transazioni.
     *
     * @param list lista di transazioni
     */
    public void setList(ArrayList<ITransaction> list) {
        budgetManagement.setList(list);
    }

    /**
     * Imposta un budget per una categoria/tag.
     *
     * @param category  nome della categoria
     * @param amount    importo del budget
     * @param startDate data di inizio
     * @param endDate   data di fine
     */
    public void setBudget(String category, double amount, LocalDate startDate, LocalDate endDate) {
        budgetManager.setBudget(category, amount, startDate, endDate);
    }

    /**
     * Restituisce tutti i budget correnti.
     *
     * @return mappa categoria -> budget
     */
    public Map<String, Budget> getAllBudgets() {
        return budgetManager.getAllBudgets();
    }

    /**
     * Aggiorna tutti i budget tramite {@link BudgetManager}.
     */
    public void updateBudgets() {
        budgetManager.updateAllBudgets();
    }

    /**
     * Restituisce la lista dei budget superati.
     *
     * @return lista di budget superati
     */
    public List<Budget> getExceededBudgets() {
        return budgetManager.getExceededBudgets();
    }

    /**
     * Rimuove un budget specifico per categoria.
     *
     * @param category nome della categoria
     */
    public void removeBudget(String category) {
        budgetManager.removeBudget(category);
    }

    // ===== TRANSAZIONI PROGRAMMATE =====

    public void addScheduledTransaction(ScheduledTransaction transaction) {
        scheduledTransactionManager.addScheduledTransaction(transaction);
    }

    public void removeScheduledTransaction(int index) {
        scheduledTransactionManager.removeScheduledTransaction(index);
    }

    public List<ScheduledTransaction> getScheduledTransactions() {
        return scheduledTransactionManager.getScheduledTransactions();
    }

    public void checkScheduledTransactions() {
        scheduledTransactionManager.checkAndExecuteScheduledTransactions();
    }

    public double calculateScheduledTransactionsForPeriod(MovementType type, LocalDate start, LocalDate end) {
        return budgetManagement.calculateScheduledTransactionsForPeriod(type, start, end);
    }

    // ===== AMMORTAMENTI =====

    /**
     * Processa tutte le rate scadute dei piani di ammortamento.
     */
    public void processAmortizationDueDates() {
        amortizationManager.processDueInstallments(this);
    }

    /**
     * Crea un piano di ammortamento.
     *
     * @param description         descrizione del piano
     * @param principalAmount     importo principale
     * @param annualInterestRate  tasso annuo
     * @param numberOfInstallments numero di rate
     * @param startDate           data di inizio
     * @param tags                lista dei tag associati
     * @return il piano di ammortamento creato
     */
    public AmortizationPlan createAmortizationPlan(String description, double principalAmount,
                                                   double annualInterestRate, int numberOfInstallments,
                                                   LocalDate startDate, List<ITag> tags) {
        return amortizationManager.createAmortizationPlan(description, principalAmount,
                annualInterestRate, numberOfInstallments,
                startDate, tags);
    }

    public List<AmortizationPlan> getAmortizationPlans() {
        return amortizationManager.getAmortizationPlans();
    }

    public boolean deleteAmortizationPlan(String planId) {
        return amortizationManager.deleteAmortizationPlan(planId);
    }

    public boolean deleteAmortizationPlan(AmortizationPlan plan) {
        return amortizationManager.deleteAmortizationPlan(plan);
    }

    /**
     * Calcola il totale delle rate di ammortamento in un periodo.
     *
     * @param startDate data di inizio
     * @param endDate data di fine
     * @return totale rate di ammortamento (valore negativo perché sono spese)
     */
    public double calculateAmortizationPaymentsForPeriod(LocalDate startDate, LocalDate endDate) {
        return amortizationManager.calculateFutureInstallments(startDate, endDate);
    }

    /**
     * Conta il numero di rate di ammortamento in un periodo.
     *
     * @param startDate data di inizio
     * @param endDate data di fine
     * @return numero di rate
     */
    public int countAmortizationPaymentsForPeriod(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        for (AmortizationPlan plan : amortizationManager.getAmortizationPlans()) {
            for (Installment installment : plan.getInstallments()) {
                if (!installment.isPaid() &&
                        !installment.getDueDate().isBefore(startDate) &&
                        !installment.getDueDate().isAfter(endDate)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Ottiene le rate di ammortamento in un periodo.
     *
     * @param startDate data di inizio
     * @param endDate data di fine
     * @return lista di rate
     */
    public List<Installment> getAmortizationInstallmentsForPeriod(LocalDate startDate, LocalDate endDate) {
        List<Installment> installments = new ArrayList<>();
        for (AmortizationPlan plan : amortizationManager.getAmortizationPlans()) {
            for (Installment installment : plan.getInstallments()) {
                if (!installment.isPaid() &&
                        !installment.getDueDate().isBefore(startDate) &&
                        !installment.getDueDate().isAfter(endDate)) {
                    installments.add(installment);
                }
            }
        }
        return installments;
    }

    // ===== SCADENZE =====

    public List<Deadline> getAllDeadlines() {
        return deadlineManager.getAllDeadlines();
    }

    public List<Deadline> getOverdueDeadlines() {
        return deadlineManager.getOverdueDeadlines();
    }

    public List<Deadline> getDueTodayDeadlines() {
        return deadlineManager.getDueTodayDeadlines();
    }

    public List<Deadline> getUpcomingDeadlines() {
        return deadlineManager.getUpcomingDeadlines();
    }

    public List<Deadline> getDeadlinesByType(DeadlineType type) {
        return deadlineManager.getDeadlinesByType(type);
    }
}
