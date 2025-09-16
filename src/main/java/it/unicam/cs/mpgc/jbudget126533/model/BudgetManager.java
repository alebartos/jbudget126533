package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * Manager dei budget che utilizza IFileManagement per la persistenza.
 */
public class BudgetManager extends BaseManager<Budget> {

    /** Mappa dei budget per categoria */
    private final Map<String, Budget> budgets = new HashMap<>();

    /** Gestore delle transazioni */
    private final IBudgetManagement budgetManagement;

    private final Ledger ledger;

    /**
     * Costruttore del BudgetManager.
     * Carica i budget esistenti, pulisce quelli scaduti e aggiorna gli importi spesi.
     *
     * @param budgetManagement oggetto per il calcolo dei saldi delle transazioni
     * @param ledger ledger utilizzato per registrare le transazioni
     */

    public BudgetManager(IBudgetManagement budgetManagement, Ledger ledger, IFileManagement fileManagement) {
        super(fileManagement, FilePaths.getFileNameOnly(FilePaths.BUDGET_FILE));
        this.budgetManagement = budgetManagement;
        this.ledger = ledger;
        loadItems();
        cleanupExpiredBudgets();
        updateAllBudgets();
    }

    @Override
    protected void loadItems() {
        try {
            Type type = new TypeToken<List<BudgetData>>() {}.getType();
            List<BudgetData> loadedData = fileManagement.readObject(fileName, type);

            if (loadedData != null) {
                managedItems.clear();
                for (BudgetData data : loadedData) {
                    managedItems.put(data.category, data.toBudget());
                }
                updateAllBudgets();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei budget: " + e.getMessage());
        }
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
        addItem(category, budget);
    }

    /**
     * Aggiorna i budget quando viene aggiunta una nuova transazione di tipo SPESA.
     *
     * @param transaction la nuova transazione aggiunta
     */
    public void onNewTransactionAdded(ITransaction transaction) {
        if (transaction.getType() == MovementType.SPESA) {
            updateAllBudgets();
            saveItems();
        }
    }

    /**
     * Aggiorna l'importo speso per un budget specifico calcolandolo dalle transazioni.
     *
     * @param category categoria del budget da aggiornare
     */
    public void updateBudgetSpentAmount(String category) {
        Budget budget = managedItems.get(category);
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
        for (String category : managedItems.keySet()) {
            updateBudgetSpentAmount(category);
        }
        saveItems();
    }

    /**
     * Restituisce il budget per una categoria specifica.
     *
     * @param category categoria del budget
     * @return oggetto {@link Budget} corrispondente, o null se non esiste
     */
    public Budget getBudget(String category) {
        return managedItems.get(category);
    }

    /**
     * Restituisce una copia di tutti i budget gestiti.
     *
     * @return mappa dei budget
     */
    public Map<String, Budget> getAllBudgets() {
        return new HashMap<>(managedItems);
    }

    /**
     * Rimuove un budget per categoria e salva immediatamente i dati.
     *
     * @param category nome della categoria
     */
    public void removeBudget(String category) {
        removeItem(category);
    }

    /**
     * Restituisce la lista dei budget che hanno superato l'importo allocato.
     *
     * @return lista di budget ecceduti
     */
    public List<Budget> getExceededBudgets() {
        List<Budget> exceeded = new ArrayList<>();
        for (Budget budget : managedItems.values()) {
            if (budget.isExceeded()) {
                exceeded.add(budget);
            }
        }
        return exceeded;
    }

    // ==================== PERSISTENZA ====================

    /**
     * Rimuove i budget scaduti (la cui data di fine è passata) e salva le modifiche.
     */
    public void cleanupExpiredBudgets() {
        managedItems.entrySet().removeIf(entry -> entry.getValue().getEndDate().isBefore(LocalDate.now()));
        saveItems();
    }


}
