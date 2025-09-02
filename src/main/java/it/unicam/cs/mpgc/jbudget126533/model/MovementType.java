package it.unicam.cs.mpgc.jbudget126533.model;

/**
 * Enumerazione che rappresenta la tipologia di movimento finanziario in una transazione.
 * <p>
 * Ogni transazione ({@link ITransaction}) è classificata come:
 * <ul>
 *   <li>{@link #GUADAGNO} → rappresenta un'entrata (aumento del saldo);</li>
 *   <li>{@link #SPESA} → rappresenta un'uscita (diminuzione del saldo).</li>
 * </ul>
 * </p>
 */
public enum MovementType {

    /**
     * Movimento che rappresenta un guadagno (entrata di denaro).
     * L'importo associato sarà generalmente positivo.
     */
    GUADAGNO,

    /**
     * Movimento che rappresenta una spesa (uscita di denaro).
     * L'importo associato sarà generalmente negativo.
     */
    SPESA
}
