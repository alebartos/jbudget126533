package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;

/**
 * Rappresenta una singola rata di un piano di ammortamento.
 * <p>
 * Ogni rata contiene informazioni su:
 * <ul>
 *   <li>Numero progressivo</li>
 *   <li>Data di scadenza</li>
 *   <li>Quota capitale e quota interessi</li>
 *   <li>Importo totale della rata</li>
 *   <li>Saldo residuo dopo il pagamento</li>
 *   <li>Stato di pagamento</li>
 *   <li>ID del piano di ammortamento a cui appartiene</li>
 * </ul>
 * </p>
 */
public class Installment {
    private int number;
    private LocalDate dueDate;
    private double principalAmount;
    private double interestAmount;
    private double totalAmount;
    private boolean paid;

    /**
     * Costruttore vuoto richiesto per la deserializzazione con Gson.
     */
    public Installment() {}

    /**
     * Costruttore completo.
     *
     * @param number numero progressivo della rata
     * @param dueDate data di scadenza
     * @param principalAmount quota capitale
     * @param interestAmount quota interessi
     * @param totalAmount importo totale della rata
     * @param remainingBalance saldo residuo dopo il pagamento
     * @param paid indica se la rata è già stata pagata
     * @param planId identificativo del piano di ammortamento
     */
    public Installment(int number, LocalDate dueDate, double principalAmount,
                       double interestAmount, double totalAmount,
                       double remainingBalance, boolean paid, String planId) {
        this.number = number;
        this.dueDate = dueDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.totalAmount = totalAmount;
        this.paid = paid;
    }

    // ==================== GETTERS ====================

    /** @return numero progressivo della rata */
    public int getNumber() { return number; }

    /** @return data di scadenza della rata */
    public LocalDate getDueDate() { return dueDate; }

    /** @return quota interessi della rata */
    public double getInterestAmount() { return interestAmount; }

    /** @return importo totale della rata */
    public double getTotalAmount() { return totalAmount; }

    /** @return true se la rata è già stata pagata */
    public boolean isPaid() { return paid; }

    /**
     * Verifica se la rata è scaduta o in scadenza oggi e non ancora pagata.
     *
     * @return true se la rata è dovuta, false altrimenti
     */
    public boolean isDue() {
        return (LocalDate.now().isAfter(dueDate) || LocalDate.now().isEqual(dueDate)) && !paid;
    }

    // ==================== SETTERS ====================

    public void setPaid(boolean paid) { this.paid = paid; }

    @Override
    public String toString() {
        return String.format("Rata %d: %.2f€ (C: %.2f€, I: %.2f€) - Scadenza: %s",
                number, totalAmount, principalAmount, interestAmount, dueDate);
    }
}
