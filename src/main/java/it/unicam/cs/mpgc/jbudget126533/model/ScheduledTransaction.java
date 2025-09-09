package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rappresenta una transazione programmata che viene eseguita automaticamente a intervalli regolari.
 * <p>
 * Ogni transazione programmata ha una descrizione, un importo, un tipo di movimento (entrata/uscita),
 * una lista di tag per la categorizzazione, una data di inizio, una data di fine e una ricorrenza.
 * </p>
 */
public class ScheduledTransaction extends Transaction {
    private final String description;
    private RecurrenceType recurrence;
    private LocalDate startDate;
    private LocalDate nextExecutionDate;
    private LocalDate endDate;
    private boolean active;
    private List<String> tagNames;


    public ScheduledTransaction() {
        super();
        this.active = true;
        this.description = "";
        this.tagNames = new ArrayList<>();
    }

    /**
     * Costruisce una transazione programmata.
     *
     * @param description Descrizione della transazione
     * @param amount Importo della transazione
     * @param type Tipo di movimento (GUADAGNO o SPESA)
     * @param tags Lista dei tag associati
     * @param recurrence Frequenza di ricorrenza
     * @param startDate Data di inizio
     * @param endDate Data di fine (può essere null)
     */
    public ScheduledTransaction(String description, double amount, MovementType type,
                                List<ITag> tags, RecurrenceType recurrence,
                                LocalDate startDate, LocalDate endDate) {
        super(type, new Person("Sistema"), amount, startDate, tags);
        this.recurrence = recurrence;
        this.startDate = startDate;
        this.description = description;
        this.nextExecutionDate = startDate;
        this.endDate = endDate;
        this.active = true;
        this.tagNames = tags.stream().map(ITag::getName).collect(Collectors.toList());
    }

    /**
     * Ricostruisce la lista dei tag dopo la deserializzazione dai nomi.
     *
     * @param allTags Mappa dei tag esistenti per nome
     */
    public void rebuildTags(Map<String, ITag> allTags) {
        if (tagNames != null && allTags != null) {
            this.tags = tagNames.stream()
                    .map(allTags::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            this.tags = new ArrayList<>();
        }
    }

    // ==================== GETTERS ====================

    public String getDescription() { return description; }
    public double getAmount() { return getMoney(); }
    public RecurrenceType getRecurrence() { return recurrence; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }

    /**
     * Restituisce i tag associati alla transazione.
     *
     * @return Lista dei tag
     */
    public List<ITag> getTags() {
        if (tags == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(tags);
    }

    /**
     * Controlla se la transazione contiene un tag specifico.
     *
     * @param tagName Nome del tag da verificare
     * @return true se il tag è presente
     */
    public boolean hasTag(String tagName) {
        return getTags().stream().anyMatch(tag -> tag.getName().equalsIgnoreCase(tagName));
    }

    /**
     * Esegue la transazione programmata se la data di esecuzione è arrivata.
     * Calcola inoltre la prossima data di esecuzione.
     *
     * @return La transazione generata, o null se non ancora eseguibile
     */
    public ITransaction execute() {
        if (!active || nextExecutionDate.isAfter(LocalDate.now())) {
            return null;
        }

        // Gestisce correttamente il segno per le spese
        double transactionAmount = getMoney();
        if (getType() == MovementType.SPESA) {
            transactionAmount = -Math.abs(getMoney()); // Forza negativo per le spese
        } else if (getType() == MovementType.GUADAGNO) {
            transactionAmount = Math.abs(getMoney()); // Forza positivo per i guadagni
        }

        Person transactionPerson = new Person("Transazione Programmata");

        Transaction transaction = new Transaction(
                getType(),
                transactionPerson,
                transactionAmount,
                nextExecutionDate,
                getTags()
        );

        calculateNextExecutionDate();

        return transaction;
    }

    /**
     * Aggiorna la prossima data di esecuzione in base alla ricorrenza.
     * Disattiva la transazione se si supera la data di fine.
     */
    private void calculateNextExecutionDate() {
        switch (recurrence) {
            case GIORNALIERO:
                nextExecutionDate = nextExecutionDate.plusDays(1);
                break;
            case SETTIMANALE:
                nextExecutionDate = nextExecutionDate.plusWeeks(1);
                break;
            case MENSILE:
                nextExecutionDate = nextExecutionDate.plusMonths(1);
                break;
            case ANNUALE:
                nextExecutionDate = nextExecutionDate.plusYears(1);
                break;
        }

        if (endDate != null && nextExecutionDate.isAfter(endDate)) {
            active = false;
        }
    }
}
