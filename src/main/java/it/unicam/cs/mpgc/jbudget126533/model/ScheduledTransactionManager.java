package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gestore delle transazioni programmate.
 * <p>
 * Questa classe si occupa di aggiungere, rimuovere, eseguire e persistere
 * tutte le transazioni programmate dell'applicazione. Le transazioni vengono
 * salvate su file JSON e possono essere eseguite automaticamente in base
 * alla loro data di esecuzione.
 * </p>
 */
public class ScheduledTransactionManager {

    private final List<ScheduledTransaction> scheduledTransactions = new ArrayList<>();
    private final Ledger ledger;
    private final IFileManagement fileManagement;

    /** Percorso del file JSON per la persistenza delle transazioni programmate */
    private static final String SCHEDULED_FILE = FilePaths.SCHEDULED_FILE;

    /**
     * Costruisce un nuovo gestore di transazioni programmate.
     *
     * @param ledger Riferimento al ledger principale per registrare le transazioni eseguite
     */
    public ScheduledTransactionManager(Ledger ledger, IFileManagement fileManagement) {
        this.ledger = ledger;
        this.fileManagement = fileManagement;
        loadScheduledTransactions();
    }

    /**
     * Aggiunge una nuova transazione programmata e salva lo stato su file.
     *
     * @param transaction La transazione da aggiungere
     */
    public void addScheduledTransaction(ScheduledTransaction transaction) {
        scheduledTransactions.add(transaction);
        saveScheduledTransactions();
    }

    /**
     * Rimuove una transazione programmata in base all'indice nella lista.
     *
     * @param index Indice della transazione da rimuovere
     */
    public void removeScheduledTransaction(int index) {
        if (index >= 0 && index < scheduledTransactions.size()) {
            scheduledTransactions.remove(index);
            saveScheduledTransactions();
        }
    }

    /**
     * Restituisce una copia della lista di tutte le transazioni programmate.
     *
     * @return Lista di transazioni programmate
     */
    public List<ScheduledTransaction> getScheduledTransactions() {
        return new ArrayList<>(scheduledTransactions);
    }

    /**
     * Controlla tutte le transazioni programmate e le esegue se la data di esecuzione è arrivata.
     * <p>
     * Ogni transazione eseguita viene registrata nel ledger e salvata su file.
     * </p>
     */
    public void checkAndExecuteScheduledTransactions() {
        boolean executed = false;
        LocalDate today = LocalDate.now();

        for (ScheduledTransaction scheduled : scheduledTransactions) {
            if (scheduled.isActive() && !scheduled.getNextExecutionDate().isAfter(today)) {
                ITransaction transaction = scheduled.execute();
                if (transaction != null) {
                    ledger.addTransaction(transaction); // Notifica automaticamente il BudgetManager
                    ledger.write(transaction);
                    executed = true;
                    System.out.println("Eseguita transazione programmata: " + scheduled.getDescription());
                }
            }
        }

        if (executed) {
            saveScheduledTransactions();
        }
    }

    // ==================== PERSISTENZA ====================

    /**
     * Salva tutte le transazioni programmate su file JSON.
     */
    public void saveScheduledTransactions() {
        try {
            fileManagement.writeObject(FilePaths.getFileNameOnly(FilePaths.SCHEDULED_FILE), scheduledTransactions);
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio transazioni programmate: " + e.getMessage());
        }
    }

    /**
     * Carica le transazioni programmate dal file JSON.
     * <p>
     * In caso di errore di lettura, la lista interna viene azzerata.
     * </p>
     */
    private void loadScheduledTransactions() {
        try {
            Type type = new TypeToken<List<ScheduledTransaction>>() {}.getType();
            List<ScheduledTransaction> loaded = fileManagement.readObject(
                    FilePaths.getFileNameOnly(FilePaths.SCHEDULED_FILE), type);

            if (loaded != null) {
                scheduledTransactions.clear();
                scheduledTransactions.addAll(loaded);
                rebuildTagsInScheduledTransactions();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento transazioni programmate: " + e.getMessage());
            scheduledTransactions.clear();
        }
    }

    /**
     * Ricostruisce i tag delle transazioni programmate usando il TagManager.
     */
    private void rebuildTagsInScheduledTransactions() {
        Map<String, ITag> allTags = TagManager.getAllTagsMap();
        for (ScheduledTransaction transaction : scheduledTransactions) {
            transaction.rebuildTags(allTags);
        }
    }

    /**
     * Reset delle transazioni programmate.
     * <p>
     * Se il file esiste, viene creato un backup con timestamp e la lista interna viene svuotata.
     * </p>
     */
    public void resetScheduledTransactions() {
        try {
            File file = new File(SCHEDULED_FILE);
            if (file.exists()) {
                File backup = new File(SCHEDULED_FILE + ".backup." + System.currentTimeMillis());
                Files.move(file.toPath(), backup.toPath());
                System.out.println("File scheduled transactions corrotto, creato backup: " + backup.getName());
            }
            scheduledTransactions.clear();
        } catch (Exception e) {
            System.err.println("Errore nel reset: " + e.getMessage());
        }
    }
}
