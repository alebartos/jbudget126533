package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestore centrale per lo scadenzario.
 * <p>
 * Questa classe raccoglie tutte le scadenze provenienti da:
 * <ul>
 *     <li>{@link ScheduledTransactionManager} → transazioni programmate</li>
 *     <li>{@link AmortizationManager} → rate di ammortamento</li>
 *     <li>{@link BudgetManager} → eventuali scadenze relative ai budget</li>
 * </ul>
 * Consente di ottenere una lista ordinata di tutte le scadenze,
 * nonché filtrarle per stato o tipologia.
 */
public class DeadlineManager {

    /** Gestore delle transazioni programmate */
    private final ScheduledTransactionManager scheduledTransactionManager;

    /** Gestore dei piani di ammortamento */
    private final AmortizationManager amortizationManager;

    /** Gestore dei budget (opzionale) */
    private final BudgetManager budgetManager;

    /**
     * Costruttore principale.
     *
     * @param scheduledTransactionManager gestore delle transazioni programmate
     * @param amortizationManager gestore dei piani di ammortamento
     * @param budgetManager gestore dei budget
     */
    public DeadlineManager(ScheduledTransactionManager scheduledTransactionManager,
                           AmortizationManager amortizationManager,
                           BudgetManager budgetManager) {
        this.scheduledTransactionManager = scheduledTransactionManager;
        this.amortizationManager = amortizationManager;
        this.budgetManager = budgetManager;
    }

    /**
     * Recupera tutte le scadenze da tutte le fonti disponibili.
     * <p>
     * L’elenco risultante viene ordinato per data crescente.
     *
     * @return lista ordinata di tutte le scadenze
     */
    public List<Deadline> getAllDeadlines() {
        List<Deadline> deadlines = new ArrayList<>();

        // Aggiungi transazioni programmate
        deadlines.addAll(getScheduledTransactionDeadlines());

        // Aggiungi rate di ammortamento
        deadlines.addAll(getAmortizationDeadlines());

        // Eventuali scadenze di budget (attualmente non implementato)
        // deadlines.addAll(getBudgetDeadlines());

        // Ordina per data
        deadlines.sort(Comparator.comparing(Deadline::getDueDate));

        return deadlines;
    }

    /**
     * Genera le scadenze relative alle transazioni programmate attive.
     *
     * @return lista di scadenze per le transazioni programmate
     */
    private List<Deadline> getScheduledTransactionDeadlines() {
        List<Deadline> deadlines = new ArrayList<>();

        if (scheduledTransactionManager != null) {
            for (ScheduledTransaction scheduled : scheduledTransactionManager.getScheduledTransactions()) {
                if (scheduled.isActive() && scheduled.getNextExecutionDate() != null) {
                    deadlines.add(new Deadline(
                            scheduled.getDescription(),
                            scheduled.getNextExecutionDate(),
                            scheduled.getAmount(),
                            scheduled.getType(),
                            DeadlineType.SCHEDULED_TRANSACTION,
                            "SCHED_" + scheduled.hashCode() // ID pseudo-unico
                    ));
                }
            }
        }

        return deadlines;
    }

    /**
     * Genera le scadenze relative alle rate di ammortamento non ancora pagate.
     *
     * @return lista di scadenze per le rate di ammortamento
     */
    private List<Deadline> getAmortizationDeadlines() {
        List<Deadline> deadlines = new ArrayList<>();

        if (amortizationManager != null) {
            for (AmortizationPlan plan : amortizationManager.getAmortizationPlans()) {
                for (Installment installment : plan.getInstallments()) {
                    if (!installment.isPaid()) {
                        deadlines.add(new Deadline(
                                "Rata " + installment.getNumber() + " - " + plan.getDescription(),
                                installment.getDueDate(),
                                installment.getTotalAmount(),
                                MovementType.SPESA, // Le rate sono sempre spese
                                DeadlineType.AMORTIZATION_INSTALLMENT,
                                plan.getId() + "_" + installment.getNumber()
                        ));
                    }
                }
            }
        }

        return deadlines;
    }

    // ==================== METODI PUBBLICI DI FILTRO ====================

    /**
     * Restituisce tutte le scadenze già scadute e non pagate.
     *
     * @return lista di scadenze scadute
     */
    public List<Deadline> getOverdueDeadlines() {
        return getAllDeadlines().stream()
                .filter(Deadline::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutte le scadenze con data odierna e non ancora pagate.
     *
     * @return lista di scadenze odierne
     */
    public List<Deadline> getDueTodayDeadlines() {
        return getAllDeadlines().stream()
                .filter(Deadline::isDueToday)
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutte le scadenze future entro i prossimi 30 giorni.
     *
     * @return lista di scadenze imminenti
     */
    public List<Deadline> getUpcomingDeadlines() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return getAllDeadlines().stream()
                .filter(deadline -> deadline.isFuture() &&
                        !deadline.getDueDate().isAfter(thirtyDaysFromNow))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce tutte le scadenze filtrate in base al tipo.
     *
     * @param type tipo di scadenza ({@link DeadlineType})
     * @return lista di scadenze corrispondenti al tipo
     */
    public List<Deadline> getDeadlinesByType(DeadlineType type) {
        return getAllDeadlines().stream()
                .filter(deadline -> deadline.getDeadlineType() == type)
                .collect(Collectors.toList());
    }
}
