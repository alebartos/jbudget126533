package it.unicam.cs.mpgc.jbudget126533.sync;

import it.unicam.cs.mpgc.jbudget126533.model.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un pacchetto di dati utilizzato per la sincronizzazione tra istanze
 * dell'applicazione JBudget.
 * <p>
 * Contiene tutte le informazioni necessarie per replicare lo stato dell'applicazione,
 * inclusi:
 * <ul>
 *     <li>Transazioni effettuate ({@link ITransaction})</li>
 *     <li>Budget ({@link Budget})</li>
 *     <li>Transazioni programmate ({@link ScheduledTransaction})</li>
 *     <li>Piani di ammortamento ({@link AmortizationPlan})</li>
 *     <li>Tag ({@link ITag}) e tag selezionati</li>
 * </ul>
 * Ogni pacchetto contiene anche un timestamp e l'ID del dispositivo che lo ha generato.
 */
public class SyncPackage {

    /** Lista di transazioni dell'utente */
    private List<ITransaction> transactions;

    /** Lista dei budget definiti dall'utente */
    private List<Budget> budgets;

    /** Lista delle transazioni programmate */
    private List<ScheduledTransaction> scheduledTransactions;

    /** Lista dei piani di ammortamento */
    private List<AmortizationPlan> amortizationPlans;

    /** Lista di tutti i tag */
    private List<ITag> tags;

    /** Lista dei tag selezionati */
    private List<ITag> selectedTags;

    /**
     * Costruttore.
     * Inizializza tutte le liste interne come vuote.
     */
    public SyncPackage() {
        this.transactions = new ArrayList<>();
        this.budgets = new ArrayList<>();
        this.scheduledTransactions = new ArrayList<>();
        this.amortizationPlans = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.selectedTags = new ArrayList<>();
    }

    // ==================== GETTER E SETTER ====================

    /** Imposta il timestamp di creazione del pacchetto */
    public void setTimestamp(LocalDateTime timestamp) { /** Timestamp di creazione del pacchetto */
    }

    /** Imposta l'ID del dispositivo che ha generato il pacchetto */
    public void setDeviceId(String deviceId) { /** ID univoco del dispositivo che ha generato il pacchetto */
    }

    /** Restituisce la lista delle transazioni */
    public List<ITransaction> getTransactions() { return transactions; }

    /** Imposta la lista delle transazioni */
    public void setTransactions(List<ITransaction> transactions) { this.transactions = transactions; }

    /** Restituisce la lista dei budget */
    public List<Budget> getBudgets() { return budgets; }

    /** Imposta la lista dei budget */
    public void setBudgets(List<Budget> budgets) { this.budgets = budgets; }

    /** Restituisce la lista delle transazioni programmate */
    public List<ScheduledTransaction> getScheduledTransactions() { return scheduledTransactions; }

    /** Imposta la lista delle transazioni programmate */
    public void setScheduledTransactions(List<ScheduledTransaction> scheduledTransactions) { this.scheduledTransactions = scheduledTransactions; }

    /** Restituisce la lista dei piani di ammortamento */
    public List<AmortizationPlan> getAmortizationPlans() { return amortizationPlans; }

    /** Imposta la lista dei piani di ammortamento */
    public void setAmortizationPlans(List<AmortizationPlan> amortizationPlans) { this.amortizationPlans = amortizationPlans; }

    /** Restituisce la lista di tutti i tag */
    public List<ITag> getTags() { return tags; }

    /** Imposta la lista di tutti i tag */
    public void setTags(List<ITag> tags) { this.tags = tags; }

    /** Restituisce la lista dei tag selezionati */
    public List<ITag> getSelectedTags() { return selectedTags; }

    /** Imposta la lista dei tag selezionati */
    public void setSelectedTags(List<ITag> selectedTags) { this.selectedTags = selectedTags; }
}
