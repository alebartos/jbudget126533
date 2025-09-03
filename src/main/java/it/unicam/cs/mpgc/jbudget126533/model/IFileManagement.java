package it.unicam.cs.mpgc.jbudget126533.model;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Interfaccia per la gestione della persistenza delle transazioni e oggetti generici.
 */
public interface IFileManagement {

    /**
     * Scrive una nuova transazione nel file di persistenza.
     */
    void write(ITransaction transaction);

    /**
     * Legge tutte le transazioni memorizzate nel file di persistenza.
     */
    ArrayList<ITransaction> read();

    /**
     * Scrive un oggetto generico in un file specifico.
     *
     * @param fileName nome del file
     * @param object oggetto da serializzare
     */
    void writeObject(String fileName, Object object);

    /**
     * Legge un oggetto generico da un file specifico.
     *
     * @param fileName nome del file
     * @param type tipo dell'oggetto da deserializzare
     * @return oggetto deserializzato
     */
    <T> T readObject(String fileName, Class<T> type);

    /**
     * Legge un oggetto generico da un file specifico usando Type (per collezioni generiche).
     *
     * @param fileName nome del file
     * @param type tipo dell'oggetto da deserializzare
     * @return oggetto deserializzato
     */
    <T> T readObject(String fileName, Type type);
}