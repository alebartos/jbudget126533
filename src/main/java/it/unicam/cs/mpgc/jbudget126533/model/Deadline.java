package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;

/**
 * Rappresenta una scadenza nel sistema finanziario.
 * Una scadenza può essere:
 * <ul>
 *     <li>Una {@link ScheduledTransaction transazione programmata}</li>
 *     <li>Una rata di un piano di ammortamento</li>
 * </ul>
 * Ogni scadenza è caratterizzata da una data, un importo, un tipo di movimento
 * e uno stato che indica se è stata pagata, scaduta o futura.
 */
public class Deadline implements Comparable<Deadline> {

    /** Descrizione della scadenza */
    private String description;

    /** Data di scadenza */
    private LocalDate dueDate;

    /** Importo della scadenza */
    private double amount;

    /** Tipo di movimento (entrata o uscita) */
    private MovementType type;

    /** Tipo di scadenza (programmata o ammortamento) */
    private DeadlineType deadlineType;

    /** Identificativo dell'elemento sorgente (transazione o piano di ammortamento) */
    private String sourceId;

    /** Stato del pagamento */
    private boolean isPaid;

    /**
     * Costruttore della scadenza.
     *
     * @param description descrizione della scadenza
     * @param dueDate data di scadenza
     * @param amount importo della scadenza
     * @param type tipo di movimento ({@link MovementType})
     * @param deadlineType tipo di scadenza ({@link DeadlineType})
     * @param sourceId identificativo della sorgente (es. ID transazione o piano)
     */
    public Deadline(String description, LocalDate dueDate, double amount,
                    MovementType type, DeadlineType deadlineType, String sourceId) {
        this.description = description;
        this.dueDate = dueDate;
        this.amount = amount;
        this.type = type;
        this.deadlineType = deadlineType;
        this.sourceId = sourceId;
        this.isPaid = false;
    }

    // ==================== GETTERS ====================

    /** @return descrizione della scadenza */
    public String getDescription() { return description; }

    /** @return data di scadenza */
    public LocalDate getDueDate() { return dueDate; }

    /** @return importo della scadenza */
    public double getAmount() { return amount; }

    /** @return tipo di movimento della scadenza */
    public MovementType getType() { return type; }

    /** @return tipo della scadenza */
    public DeadlineType getDeadlineType() { return deadlineType; }

    /** @return identificativo della sorgente */
    public String getSourceId() { return sourceId; }

    /** @return true se la scadenza è stata pagata */
    public boolean isPaid() { return isPaid; }

    /**
     * Verifica se la scadenza è scaduta e non ancora pagata.
     *
     * @return true se scaduta e non pagata
     */
    public boolean isOverdue() { return LocalDate.now().isAfter(dueDate) && !isPaid; }

    /**
     * Verifica se la scadenza è oggi e non ancora pagata.
     *
     * @return true se la scadenza è oggi
     */
    public boolean isDueToday() { return LocalDate.now().equals(dueDate) && !isPaid; }

    /**
     * Verifica se la scadenza è futura e non ancora pagata.
     *
     * @return true se futura e non pagata
     */
    public boolean isFuture() { return LocalDate.now().isBefore(dueDate) && !isPaid; }

    // ==================== SETTERS ====================

    /**
     * Imposta lo stato di pagamento della scadenza.
     *
     * @param paid true se la scadenza è stata pagata
     */
    public void setPaid(boolean paid) { isPaid = paid; }

    // ==================== OVERRIDE ====================

    /**
     * Confronta due scadenze in base alla data.
     *
     * @param other altra scadenza da confrontare
     * @return risultato del confronto tra le date
     */
    @Override
    public int compareTo(Deadline other) {
        return this.dueDate.compareTo(other.dueDate);
    }

    /**
     * Restituisce una rappresentazione testuale della scadenza,
     * con stato (pagata, scaduta, odierna, futura), data, tipo e importo.
     *
     * @return stringa descrittiva della scadenza
     */
    @Override
    public String toString() {
        String status = isPaid ? "✅ Pagato" : isOverdue() ? "⚠️ Scaduto" : isDueToday() ? "📅 Oggi" : "⏳ Futuro";
        return String.format("%s | %s | %s: %.2f€ - %s",
                dueDate, status, type, Math.abs(amount), description);
    }
}
