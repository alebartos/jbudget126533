package it.unicam.cs.mpgc.jbudget126533.model;

/**
 * Tipo di ricorrenza della transazione programmata.
 */
public enum RecurrenceType {
    /** Transazione che si ripete ogni giorno. */
    GIORNALIERO,

    /** Transazione che si ripete ogni settimana. */
    SETTIMANALE,

    /** Transazione che si ripete ogni mese. */
    MENSILE,

    /** Transazione che si ripete ogni anno. */
    ANNUALE
}