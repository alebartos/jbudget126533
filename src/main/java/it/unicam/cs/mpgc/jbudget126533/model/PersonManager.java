package it.unicam.cs.mpgc.jbudget126533.model;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Gestisce la raccolta di oggetti Person, offrendo metodi statici per aggiungere,
 * recuperare, eliminare e ottenere tutte le persone.
 * Si occupa inoltre del caricamento e salvataggio persistente da file JSON.
 */
public class PersonManager {
    private static final String PERSON_FILE = "Persons.json";
    private static final Map<String, Person> persons = new HashMap<>();
    private static IFileManagement fileManagement = new FileManagement();

    /**
     * Blocco statico che carica le persone salvate dal file all'inizializzazione della classe.
     */
    static {
        loadPersons();
    }

    /**
     * Aggiunge una nuova persona alla raccolta.
     * Salva automaticamente le modifiche su file.
     *
     * @param person l'oggetto Person da aggiungere
     */
    public static void addPerson(Person person) {
        persons.put(person.getName().toLowerCase(), person);
        savePersons();
    }

    /**
     * Recupera una persona dall'archivio a partire dal nome (case-insensitive).
     *
     * @param name il nome della persona da cercare
     * @return l'oggetto Person corrispondente, oppure null se non presente
     */
    public static Person getPerson(String name) {
        return persons.get(name.toLowerCase());
    }

    /**
     * Restituisce una lista di tutte le persone presenti nell'archivio.
     *
     * @return lista di tutte le persone
     */
    public static List<Person> getAllPersons() {
        return new ArrayList<>(persons.values());
    }

    /**
     * Rimuove una persona dall'archivio tramite il suo nome (case-insensitive).
     * Salva automaticamente le modifiche su file se la rimozione ha successo.
     *
     * @param name il nome della persona da rimuovere
     * @return true se la persona era presente ed è stata rimossa, false altrimenti
     */
    public static boolean removePerson(String name) {
        if (persons.remove(name.toLowerCase()) != null) {
            savePersons();
            return true;
        }
        return false;
    }

    /**
     * Carica la lista delle persone dal file JSON e aggiorna la mappa interna.
     * In caso di errori stampa un messaggio sullo standard error.
     */
    private static void loadPersons() {
        try {
            Type type = new TypeToken<List<Person>>() {}.getType();
            List<Person> loadedPersons = fileManagement.readObject(PERSON_FILE, type);

            if (loadedPersons != null) {
                persons.clear();
                for (Person person : loadedPersons) {
                    persons.put(person.getName().toLowerCase(), person);
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle persone: " + e.getMessage());
        }
    }

    /**
     * Salva la lista corrente delle persone nel file JSON.
     * In caso di errori stampa un messaggio sullo standard error.
     */
    private static void savePersons() {
        try {
            fileManagement.writeObject(PERSON_FILE, new ArrayList<>(persons.values()));
        } catch (Exception e) {
            System.err.println("Errore nel salvataggio delle persone: " + e.getMessage());
        }
    }
}
