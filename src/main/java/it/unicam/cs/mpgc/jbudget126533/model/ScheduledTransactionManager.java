package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.reflect.TypeToken;
import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;

import java.lang.reflect.Type;
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
public class ScheduledTransactionManager extends BaseManager<ScheduledTransaction> {
    private final Ledger ledger;
    /**
     * Costruisce un nuovo gestore di transazioni programmate.
     *
     * @param ledger Riferimento al ledger principale per registrare le transazioni eseguite
     */
    public ScheduledTransactionManager(Ledger ledger, IFileManagement fileManagement) {
        super(fileManagement, FilePaths.getFileNameOnly(FilePaths.SCHEDULED_FILE));
        this.ledger = ledger;
        loadItems();
    }

    @Override
    protected void loadItems() {
        try {
            Type type = new TypeToken<List<ScheduledTransaction>>() {}.getType();
            List<ScheduledTransaction> loaded = fileManagement.readObject(fileName, type);

            if (loaded != null) {
                managedItems.clear();
                for (ScheduledTransaction transaction : loaded) {
                    String id = "SCHED_" + transaction.hashCode();
                    managedItems.put(id, transaction);
                }
                rebuildTagsInScheduledTransactions();
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento transazioni programmate: " + e.getMessage());
            managedItems.clear();
        }
    }

    /**
     * Aggiunge una nuova transazione programmata e salva lo stato su file.
     *
     * @param transaction La transazione da aggiungere
     */
    public void addScheduledTransaction(ScheduledTransaction transaction) {
        String id = "SCHED_" + transaction.hashCode();
        addItem(id, transaction);
    }

    /**
     * Rimuove una transazione programmata in base all'indice nella lista.
     *
     * @param index Indice della transazione da rimuovere
     */
    public void removeScheduledTransaction(int index) {
        if (index >= 0 && index < managedItems.size()) {
            String id = new ArrayList<>(managedItems.keySet()).get(index);
            removeItem(id);
        }
    }

    /**
     * Restituisce una copia della lista di tutte le transazioni programmate.
     *
     * @return Lista di transazioni programmate
     */
    public List<ScheduledTransaction> getScheduledTransactions() {
        return new ArrayList<>(managedItems.values());
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

        for (ScheduledTransaction scheduled : managedItems.values()) {
            if (scheduled.isActive() && !scheduled.getNextExecutionDate().isAfter(today)) {
                ITransaction transaction = scheduled.execute();
                if (transaction != null) {
                    ledger.addTransaction(transaction);
                    ledger.write(transaction);
                    executed = true;
                    System.out.println("Eseguita transazione programmata: " + scheduled.getDescription());
                }
            }
        }

        if (executed) {
            saveItems();
        }
    }

    public void saveScheduledTransactions() {
        saveItems();
    }

    /**
     * Ricostruisce i tag delle transazioni programmate usando il TagManager.
     */
    private void rebuildTagsInScheduledTransactions() {
        Map<String, ITag> allTags = TagManager.getAllTagsMap();
        for (ScheduledTransaction transaction : managedItems.values()) {
            transaction.rebuildTags(allTags);
        }
    }

}
