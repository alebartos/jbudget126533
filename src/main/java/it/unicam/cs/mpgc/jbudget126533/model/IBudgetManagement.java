package it.unicam.cs.mpgc.jbudget126533.model;

import it.unicam.cs.mpgc.jbudget126533.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Interfaccia che definisce le operazioni principali per la gestione del budget.
 * <p>
 * Permette di calcolare saldi globali e filtrati, gestire transazioni,
 * e integrare transazioni programmate e piani di ammortamento.
 */
public interface IBudgetManagement {

    /**
     * Restituisce il saldo totale considerando tutte le transazioni registrate.
     *
     * @return saldo totale
     */
    double getBalance();

    /**
     * Inserisce una nuova transazione nella lista.
     *
     * @param transaction transazione da aggiungere
     */
    void insert(ITransaction transaction);

    /**
     * Restituisce una funzione che calcola l’andamento del saldo
     * in un intervallo di tempo.
     * <p>
     * La funzione ritorna una {@link Pair}:
     * <ul>
     *   <li>{@link Boolean} → indica se il trend è positivo (true) o negativo (false)</li>
     *   <li>{@link Double} → valore numerico del saldo nel periodo</li>
     * </ul>
     *
     * @param dateStart data di inizio (inclusa)
     * @param dateEnd   data di fine (inclusa, se null usa la data odierna)
     * @return funzione di calcolo del trend
     */
    Function<ArrayList<ITransaction>, Pair<Boolean, Double>> trendBalance(LocalDate dateStart, LocalDate dateEnd);

    /**
     * Calcola il saldo per un tag specifico.
     *
     * @param type tipo di movimento ({@link MovementType}) da filtrare
     * @param tag  nome del tag
     * @return saldo associato al tag
     */
    double balanceForTag(MovementType type, String tag);

    /**
     * Calcola il saldo in un intervallo di date.
     *
     * @param type      tipo di movimento da filtrare (può essere null per includere tutti)
     * @param dateStart data di inizio (inclusa)
     * @param dateEnd   data di fine (inclusa, se null usa la data odierna)
     * @return saldo calcolato nell’intervallo
     */
    double balanceForDates(MovementType type, LocalDate dateStart, LocalDate dateEnd);

    /**
     * Calcola il saldo relativo a un tag in un intervallo di date.
     *
     * @param type      tipo di movimento ({@link MovementType}) da filtrare
     * @param tag       nome del tag
     * @param dateStart data di inizio (inclusa)
     * @param dateEnd   data di fine (inclusa, se null usa la data odierna)
     * @return saldo per il tag nell’intervallo
     */
    double balanceForTag(MovementType type, String tag, LocalDate dateStart, LocalDate dateEnd);

    /**
     * Restituisce una mappa dei saldi per ciascun tag.
     *
     * @param type tipo di movimento ({@link MovementType}) da considerare
     * @return mappa {tag → saldo}
     */
    HashMap<String, Double> balanceForEachTag(MovementType type);

    /**
     * Restituisce la lista delle transazioni correnti.
     *
     * @return lista di transazioni
     */
    ArrayList<ITransaction> getList();

    /**
     * Imposta la lista di transazioni.
     *
     * @param list nuova lista di transazioni
     */
    void setList(ArrayList<ITransaction> list);

    /**
     * Imposta il gestore delle transazioni programmate.
     *
     * @param scheduledTransactionManager manager delle transazioni programmate
     */
    void setScheduledTransactionManager(ScheduledTransactionManager scheduledTransactionManager);

    /**
     * Calcola l’importo totale delle transazioni programmate
     * in un intervallo di tempo.
     *
     * @param type  tipo di movimento ({@link MovementType}) da filtrare (null per tutti)
     * @param start data di inizio (inclusa)
     * @param end   data di fine (inclusa)
     * @return importo totale delle transazioni programmate
     */
    double calculateScheduledTransactionsForPeriod(MovementType type, LocalDate start, LocalDate end);

    /**
     * Calcola l’importo totale delle transazioni programmate
     * relative a un tag in un intervallo di tempo.
     *
     * @param type  tipo di movimento ({@link MovementType}) da filtrare (null per tutti)
     * @param tag   nome del tag
     * @param start data di inizio (inclusa)
     * @param end   data di fine (inclusa)
     * @return importo totale delle transazioni programmate per il tag
     */
    double calculateScheduledTagTransactionsForPeriod(MovementType type, String tag, LocalDate start, LocalDate end);

    /**
     * Imposta il gestore dei piani di ammortamento.
     *
     * @param amortizationManager manager dei piani di ammortamento
     */
    void setAmortizationManager(AmortizationManager amortizationManager);

}
