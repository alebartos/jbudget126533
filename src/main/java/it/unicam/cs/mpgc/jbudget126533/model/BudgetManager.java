package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * Manager dei budget che utilizza IFileManagement per la persistenza.
 */
public class BudgetManager {

    /** Mappa dei budget per categoria */
    private final Map<String, Budget> budgets = new HashMap<>();

    /** Gestore delle transazioni */
    private final IBudgetManagement budgetManagement;

    /** Ledger delle transazioni */
    private final Ledger ledger;

    /** Gestore della persistenza */
    private final IFileManagement fileManagement;

    /** File di persistenza dei budget */
    private static final String BUDGET_FILE = FilePaths.BUDGET_FILE;

    /**
     * Costruttore del BudgetManager.
     * Carica i budget esistenti, pulisce quelli scaduti e aggiorna gli importi spesi.
     *
     * @param budgetManagement oggetto per il calcolo dei saldi delle transazioni
     * @param ledger ledger utilizzato per registrare le transazioni
     */

    public BudgetManager(IBudgetManagement budgetManagement, Ledger ledger, IFileManagement fileManagement) {
        this.budgetManagement = budgetManagement;
        this.ledger = ledger;
        this.fileManagement = fileManagement;
        loadBudgets();
        cleanupExpiredBudgets();
        updateAllBudgets();
    }

    /**
     * Classe interna per facilitare la serializzazione/deserializzazione dei budget.
     */
    private static class BudgetData {
        String category;
        double allocatedAmount;
        double spentAmount;
        LocalDate startDate;
        LocalDate endDate;

        BudgetData() {}

        BudgetData(Budget budget) {
            this.category = budget.getCategory();
            this.allocatedAmount = budget.getAllocatedAmount();
            this.spentAmount = budget.getSpentAmount();
            this.startDate = budget.getStartDate();
            this.endDate = budget.getEndDate();
        }

        /**
         * Converte il BudgetData in un oggetto {@link Budget}.
         *
         * @return oggetto Budget corrispondente
         */
        Budget toBudget() {
            Budget budget = new Budget(category, allocatedAmount, startDate, endDate);
            budget.setSpentAmount(spentAmount);
            return budget;
        }
    }

    /**
     * Crea o aggiorna un budget per una categoria specifica.
     * Aggiorna immediatamente l'importo speso e salva i budget.
     *
     * @param category categoria del budget
     * @param amount importo allocato
     * @param startDate data di inizio del budget
     * @param endDate data di fine del budget
     */
    public void setBudget(String category, double amount, LocalDate startDate, LocalDate endDate) {
        Budget budget = new Budget(category, amount, startDate, endDate);
        updateBudgetSpentAmount(category);
        budgets.put(category, budget);
        saveBudgets();
    }

    /**
     * Aggiorna i budget quando viene aggiunta una nuova transazione di tipo SPESA.
     *
     * @param transaction la nuova transazione aggiunta
     */
    public void onNewTransactionAdded(ITransaction transaction) {
        if (transaction.getType() == MovementType.SPESA) {
            updateAllBudgets();
            saveBudgets();
        }
    }

    /**
     * Aggiorna l'importo speso per un budget specifico calcolandolo dalle transazioni.
     *
     * @param category categoria del budget da aggiornare
     */
    public void updateBudgetSpentAmount(String category) {
        Budget budget = budgets.get(category);
        if (budget != null) {
            double spent = budgetManagement.balanceForTag(
                    MovementType.SPESA,
                    category,
                    budget.getStartDate(),
                    budget.getEndDate()
            );
            budget.setSpentAmount(Math.abs(spent));
        }
    }

    /**
     * Aggiorna l'importo speso per tutti i budget presenti.
     * Salva i dati aggiornati su file.
     */
    public void updateAllBudgets() {
        for (String category : budgets.keySet()) {
            updateBudgetSpentAmount(category);
        }
        saveBudgets();
    }

    /**
     * Restituisce il budget per una categoria specifica.
     *
     * @param category categoria del budget
     * @return oggetto {@link Budget} corrispondente, o null se non esiste
     */
    public Budget getBudget(String category) {
        return budgets.get(category);
    }

    /**
     * Restituisce una copia di tutti i budget gestiti.
     *
     * @return mappa dei budget
     */
    public Map<String, Budget> getAllBudgets() {
        return new HashMap<>(budgets);
    }

    /**
     * Rimuove un budget per categoria e salva immediatamente i dati.
     *
     * @param category nome della categoria
     */
    public void removeBudget(String category) {
        budgets.remove(category);
        saveBudgets();
    }

    /**
     * Restituisce la lista dei budget che hanno superato l'importo allocato.
     *
     * @return lista di budget ecceduti
     */
    public List<Budget> getExceededBudgets() {
        List<Budget> exceeded = new ArrayList<>();
        for (Budget budget : budgets.values()) {
            if (budget.isExceeded()) {
                exceeded.add(budget);
            }
        }
        return exceeded;
    }

    // ==================== PERSISTENZA ====================

    /**
     * Salva i budget attuali usando IFileManagement.
     */
    private void saveBudgets() {
        try {
            List<BudgetData> budgetDataList = new ArrayList<>();
            for (Budget budget : budgets.values()) {
                budgetDataList.add(new BudgetData(budget));
            }

            fileManagement.writeObject(FilePaths.getFileNameOnly(FilePaths.BUDGET_FILE), budgetDataList);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio dei budget: " + e.getMessage());
        }
    }

    /**
     * Carica i budget salvati usando IFileManagement.
     * Aggiorna gli importi spesi all'avvio.
     */
    private void loadBudgets() {
        try {
            Type type = new TypeToken<List<BudgetData>>() {}.getType();
            List<BudgetData> loadedData = fileManagement.readObject(
                    FilePaths.getFileNameOnly(FilePaths.BUDGET_FILE), type);

            if (loadedData != null) {
                budgets.clear();
                for (BudgetData data : loadedData) {
                    budgets.put(data.category, data.toBudget());
                }
                updateAllBudgets();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei budget: " + e.getMessage());
        }
    }

    /**
     * Rimuove i budget scaduti (la cui data di fine è passata) e salva le modifiche.
     */
    public void cleanupExpiredBudgets() {
        budgets.entrySet().removeIf(entry -> entry.getValue().getEndDate().isBefore(LocalDate.now()));
        saveBudgets();
    }
}