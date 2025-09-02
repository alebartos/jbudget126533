package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;

/**
 * Rappresenta un budget associato a una categoria per un intervallo di tempo specifico.
 * <p>
 * Permette di monitorare quanto denaro è stato speso, calcolare l'importo residuo,
 * la percentuale di utilizzo e verificare se il budget è stato superato.
 * </p>
 */
public class Budget {
    private final String category;
    private final double allocatedAmount;
    private double spentAmount;
    private final LocalDate startDate;
    private final LocalDate endDate;

    /**
     * Costruisce un nuovo budget per una categoria specifica.
     *
     * @param category        nome della categoria di spesa
     * @param allocatedAmount importo totale allocato
     * @param startDate       data di inizio del budget
     * @param endDate         data di fine del budget
     */
    public Budget(String category, double allocatedAmount,
                  LocalDate startDate, LocalDate endDate) {
        this.category = category;
        this.allocatedAmount = allocatedAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.spentAmount = 0.0;
    }

    // ===================== GETTERS =====================

    /** @return nome della categoria di spesa */
    public String getCategory() { return category; }

    /** @return importo totale allocato per il budget */
    public double getAllocatedAmount() { return allocatedAmount; }

    /** @return importo già speso */
    public double getSpentAmount() { return spentAmount; }

    /** @return data di inizio del budget */
    public LocalDate getStartDate() { return startDate; }

    /** @return data di fine del budget */
    public LocalDate getEndDate() { return endDate; }

    // ===================== SETTERS =====================

    /**
     * Aggiorna l'importo speso finora.
     *
     * @param spent importo speso
     */
    public void setSpentAmount(double spent) { this.spentAmount = spent; }

    // ===================== METODI DI UTILITÀ =====================

    /**
     * Calcola l'importo residuo disponibile nel budget.
     *
     * @return importo residuo
     */
    public double getRemainingAmount() {
        return allocatedAmount - spentAmount;
    }

    /**
     * Calcola la percentuale del budget già utilizzata.
     *
     * @return percentuale di utilizzo (0-100)
     */
    public double getUsagePercentage() {
        if (allocatedAmount == 0) return 0;
        return (spentAmount / allocatedAmount) * 100;
    }

    /**
     * Verifica se il budget è stato superato.
     *
     * @return true se l'importo speso è maggiore dell'allocazione
     */
    public boolean isExceeded() {
        return spentAmount > allocatedAmount;
    }
}
