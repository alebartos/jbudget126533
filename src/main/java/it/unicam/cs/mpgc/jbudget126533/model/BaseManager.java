package it.unicam.cs.mpgc.jbudget126533.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe base astratta per tutti i manager che gestiscono entità con ID.
 * Fornisce funzionalità comuni per la gestione delle mappe e la persistenza.
 */
public abstract class BaseManager<T> {
    protected final Map<String, T> managedItems = new HashMap<>();
    protected final IFileManagement fileManagement;
    protected final String fileName;

    protected BaseManager(IFileManagement fileManagement, String fileName) {
        this.fileManagement = fileManagement;
        this.fileName = fileName;
    }

    /**
     * Salva gli elementi gestiti nel file.
     */
    protected void saveItems() {
        try {
            fileManagement.writeObject(fileName, new ArrayList<>(managedItems.values()));
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio di " + fileName + ": " + e.getMessage());
        }
    }

    /**
     * Carica gli elementi dal file.
     */
    protected abstract void loadItems();

    /**
     * Aggiunge un elemento.
     */
    public void addItem(String id, T item) {
        managedItems.put(id, item);
        saveItems();
    }

    /**
     * Rimuove un elemento per ID.
     */
    public boolean removeItem(String id) {
        if (managedItems.containsKey(id)) {
            managedItems.remove(id);
            saveItems();
            return true;
        }
        return false;
    }

    /**
     * Ottiene un elemento per ID.
     */
    public T getItem(String id) {
        return managedItems.get(id);
    }

    /**
     * Ottiene tutti gli elementi.
     */
    public Map<String, T> getAllItems() {
        return new HashMap<>(managedItems);
    }
}