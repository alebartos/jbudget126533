package it.unicam.cs.mpgc.jbudget126533.util;

/**
 * Eccezione personalizzata lanciata quando l'input dell'utente
 * non corrisponde a una voce valida del menu o a un formato previsto.
 */
public class InvalidInputException extends Exception {

    /**
     * Costruttore che accetta un messaggio descrittivo dell'errore.
     *
     * @param message Messaggio di errore
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
