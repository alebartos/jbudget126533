package it.unicam.cs.mpgc.jbudget126533.model;
/**
 * Tipologia di scadenza:
 * <ul>
 *     <li>{@link #SCHEDULED_TRANSACTION}: transazione programmata</li>
 *     <li>{@link #AMORTIZATION_INSTALLMENT}: rata di ammortamento</li>
 * </ul>
 */
public enum DeadlineType {
    SCHEDULED_TRANSACTION,
    AMORTIZATION_INSTALLMENT
}
