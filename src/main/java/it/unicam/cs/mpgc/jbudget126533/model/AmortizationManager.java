package it.unicam.cs.mpgc.jbudget126533.model;

import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

/**
 * Gestore dei piani di ammortamento.
 * <p>
 * Questa classe si occupa della creazione, gestione e persistenza dei piani di ammortamento
 * utilizzando un oggetto {@link IFileManagement} per leggere e scrivere su file JSON.
 * Include anche metodi per processare rate scadute, calcolare rate future e gestire eliminazioni.
 */
public class AmortizationManager {

    /** Mappa dei piani di ammortamento attivi, identificati da un ID univoco. */
    private final Map<String, AmortizationPlan> amortizationPlans = new HashMap<>();

    /** Gestore della persistenza su file. */
    private final IFileManagement fileManagement;

// --Commented out by Inspection START (03/09/25, 04:37):
//    /** Nome del file JSON utilizzato per salvare i piani di ammortamento. */
//    private static final String AMORTIZATION_FILE = FilePaths.AMORTIZATION_FILE;
// --Commented out by Inspection STOP (03/09/25, 04:37)

    /**
     * Costruttore di default.
     * <p>
     * Inizializza {@link IFileManagement} e carica automaticamente i piani di ammortamento salvati.
     */
    public AmortizationManager() {
        this.fileManagement = new FileManagement();
        loadAmortizationPlans();
    }

    /**
     * Costruttore con gestore di file personalizzato.
     * <p>
     * Utile per testing o per utilizzare implementazioni diverse di {@link IFileManagement}.
     *
     * @param fileManagement gestore dei file da utilizzare
     */
    public AmortizationManager(IFileManagement fileManagement) {
        this.fileManagement = fileManagement;
        loadAmortizationPlans();
    }

    /**
     * Crea un nuovo piano di ammortamento e lo salva.
     *
     * @param description descrizione del piano
     * @param principalAmount importo principale
     * @param annualInterestRate tasso di interesse annuo in percentuale
     * @param numberOfInstallments numero di rate
     * @param startDate data di inizio
     * @param tags lista di tag associati
     * @return il piano di ammortamento creato
     */
    public AmortizationPlan createAmortizationPlan(String description, double principalAmount,
                                                   double annualInterestRate, int numberOfInstallments,
                                                   LocalDate startDate, List<ITag> tags) {
        String id = "AMORT_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        AmortizationPlan plan = new AmortizationPlan(id, description, principalAmount, annualInterestRate,
                numberOfInstallments, startDate, tags);
        amortizationPlans.put(id, plan);
        saveAmortizationPlans();
        return plan;
    }

    /**
     * Processa tutte le rate scadute dei piani di ammortamento.
     * <p>
     * Per ogni rata scaduta non pagata viene creata una transazione di tipo {@link MovementType#SPESA}
     * e aggiunta al {@link Ledger} fornito.
     *
     * @param ledger il registro contabile dove salvare le transazioni generate
     */
    public void processDueInstallments(Ledger ledger) {
        LocalDate today = LocalDate.now();
        boolean changes = false;

        for (AmortizationPlan plan : amortizationPlans.values()) {
            for (Installment installment : plan.getInstallments()) {
                if (!installment.isPaid() && installment.isDue()) {
                    ITransaction transaction = createTransactionFromInstallment(installment, plan);
                    ledger.addTransaction(transaction);
                    ledger.write(transaction);

                    installment.setPaid(true);
                    changes = true;

                    System.out.println("Processata rata scaduta: " + installment.getNumber() +
                            " - " + plan.getDescription());
                }
            }
        }

        if (changes) {
            saveAmortizationPlans();
        }
    }

    /**
     * Calcola il totale delle rate future comprese tra due date.
     *
     * @param startDate data iniziale del periodo
     * @param endDate data finale del periodo
     * @return somma totale delle rate future nel periodo
     */
    public double calculateFutureInstallments(LocalDate startDate, LocalDate endDate) {
        double total = 0;

        for (AmortizationPlan plan : amortizationPlans.values()) {
            for (Installment installment : plan.getInstallments()) {
                LocalDate dueDate = installment.getDueDate();
                if (!installment.isPaid() &&
                        !dueDate.isBefore(startDate) &&
                        !dueDate.isAfter(endDate)) {
                    total += installment.getTotalAmount();
                }
            }
        }

        return -total;
    }

    /**
     * Restituisce una lista di tutti i piani di ammortamento presenti.
     *
     * @return lista dei piani di ammortamento
     */
    public List<AmortizationPlan> getAmortizationPlans() {
        return new ArrayList<>(amortizationPlans.values());
    }

    /**
     * Salva tutti i piani di ammortamento usando {@link IFileManagement}.
     */
    public void save() {
        saveAmortizationPlans();
    }

    /**
     * Elimina un piano di ammortamento dato il suo ID.
     *
     * @param planId ID del piano da eliminare
     * @return true se il piano è stato eliminato, false altrimenti
     */
    public boolean deleteAmortizationPlan(String planId) {
        if (amortizationPlans.containsKey(planId)) {
            amortizationPlans.remove(planId);
            saveAmortizationPlans();
            System.out.println("Piano eliminato: " + planId);
            return true;
        }
        return false;
    }

    /**
     * Elimina un piano di ammortamento specificato.
     *
     * @param plan piano da eliminare
     * @return true se il piano è stato eliminato, false altrimenti
     */
    public boolean deleteAmortizationPlan(AmortizationPlan plan) {
        return deleteAmortizationPlan(plan.getId());
    }

    // ==================== METODI PRIVATI ====================

    private ITransaction createTransactionFromInstallment(Installment installment, AmortizationPlan plan) {
        Person systemPerson = new Person("Ammortamento");

        return new Transaction(
                MovementType.SPESA,
                systemPerson,
                -installment.getTotalAmount(),
                installment.getDueDate(),
                plan.getTags()
        );
    }

    private void saveAmortizationPlans() {
        try {
            List<AmortizationPlan> plansToSave = new ArrayList<>(amortizationPlans.values());
            fileManagement.writeObject(FilePaths.getFileNameOnly(FilePaths.AMORTIZATION_FILE), plansToSave);
            System.out.println("Piani di ammortamento salvati: " + plansToSave.size());
        } catch (Exception e) {
            System.err.println("Errore salvataggio piani ammortamento: " + e.getMessage());
        }
    }

    private void loadAmortizationPlans() {
        try {
            Type type = new TypeToken<List<AmortizationPlan>>() {}.getType();
            List<AmortizationPlan> loadedPlans = fileManagement.readObject(
                    FilePaths.getFileNameOnly(FilePaths.AMORTIZATION_FILE), type);

            if (loadedPlans != null) {
                amortizationPlans.clear();
                for (AmortizationPlan plan : loadedPlans) {
                    System.out.println("Caricato piano: " + plan.getDescription() + " con " +
                            plan.getInstallments().size() + " rate");
                    amortizationPlans.put(plan.getId(), plan);
                }
                System.out.println("Piani di ammortamento caricati: " + amortizationPlans.size());
            } else {
                System.out.println("Nessun piano di ammortamento trovato, inizializzo vuoto");
            }
        } catch (Exception e) {
            System.err.println("Errore caricamento piani ammortamento: " + e.getMessage());
            e.printStackTrace();
            saveAmortizationPlans(); // Crea file vuoto se non esiste
        }
    }
}
