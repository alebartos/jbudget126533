package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.ArrayList;

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
public class Installment extends Transaction {
    private int number;
    private double principalAmount;
    private double interestAmount;
    private boolean paid;
    private String planId;

    public Installment() {
        super();
        super.setType(MovementType.SPESA);
    }


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
        super(MovementType.SPESA, new Person("Sistema"), totalAmount, dueDate, new ArrayList<>());
        this.number = number;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.paid = paid;
        this.planId = planId;
    }

    // ==================== GETTERS ====================

    /** @return numero progressivo della rata */
    public int getNumber() { return number; }

    /** @return data di scadenza della rata */
    public LocalDate getDueDate() { return super.getDate(); }

    /** @return quota interessi della rata */
    public double getInterestAmount() { return interestAmount; }

    /** @return importo totale della rata */
    public double getTotalAmount() { return super.getMoney(); }

    /** @return true se la rata è già stata pagata */
    public boolean isPaid() { return paid; }

    public String getPlanId() { return planId; }
    /**
     * Verifica se la rata è scaduta o in scadenza oggi e non ancora pagata.
     *
     * @return true se la rata è dovuta, false altrimenti
     */
    public boolean isDue() {
        return (LocalDate.now().isAfter(getDueDate()) || LocalDate.now().isEqual(getDueDate())) && !paid;
    }

    // ==================== SETTERS ====================

    public void setPaid(boolean paid) { this.paid = paid; }


    @Override
    public String toString() {
        return String.format("Rata %d: %.2f€ (C: %.2f€, I: %.2f€) - Scadenza: %s",
                number, getTotalAmount(), principalAmount, interestAmount, getDueDate());
    }
}
