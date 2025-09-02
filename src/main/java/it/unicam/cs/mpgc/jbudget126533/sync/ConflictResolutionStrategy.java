package it.unicam.cs.mpgc.jbudget126533.sync;

/**
 * Strategie per la risoluzione dei conflitti durante la sincronizzazione.
 */
public enum ConflictResolutionStrategy {
    /**
     * Mantiene i dati locali in caso di conflitto
     */
    LOCAL_WINS,

    /**
     * Sostituisce con i dati remoti in caso di conflitto
     */
    REMOTE_WINS,

    /**
     * Mantiene entrambe le versioni (richiede interazione utente)
     */
    MANUAL_RESOLUTION,

    /**
     * Unisce i dati intelligente (più recente vince)
     */
    NEWEST_WINS
}