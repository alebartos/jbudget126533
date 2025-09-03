package it.unicam.cs.mpgc.jbudget126533.model;

import it.unicam.cs.mpgc.jbudget126533.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Gestisce le transazioni, i bilanci e le analisi per categorie e date.
 * Integra transazioni reali, transazioni programmate e piani di ammortamento.
 */
public class BudgetManagement implements IBudgetManagement {

    /** Lista delle transazioni già avvenute */
    private ArrayList<ITransaction> transactionsList;

    /** Manager per le transazioni programmate */
    private ScheduledTransactionManager scheduledTransactionManager;

    /** Manager per le rate di ammortamento */
    private AmortizationManager amortizationManager;

    /**
     * Imposta il manager per i piani di ammortamento.
     * @param amortizationManager il manager di ammortamento da usare
     */
    public void setAmortizationManager(AmortizationManager amortizationManager) {
        this.amortizationManager = amortizationManager;
    }

    /**
     * Costruttore vuoto.
     * Inizializza la lista delle transazioni.
     */
    public BudgetManagement() {
        this.transactionsList = new ArrayList<>();
    }

    /**
     * Restituisce il saldo attuale basato sulle transazioni reali.
     * @return saldo totale
     */
    @Override
    public double getBalance() {
        return transactionsList.stream()
                .mapToDouble(ITransaction::getMoney)
                .sum();
    }

    /**
     * Inserisce una nuova transazione nella lista delle transazioni reali.
     * @param transaction la transazione da inserire
     */
    @Override
    public void insert(ITransaction transaction) {
        transactionsList.add(transaction);
    }

    /**
     * Calcola il trend del saldo in un intervallo di date.
     * Considera transazioni reali, transazioni programmate e rate di ammortamento future.
     * @param dateStart data di inizio del periodo (può essere null)
     * @param dateEnd data di fine del periodo (può essere null)
     * @return funzione che calcola un Pair<Boolean, Double> indicante saldo positivo/negativo e valore totale
     */
    @Override
    public Function<ArrayList<ITransaction>, Pair<Boolean, Double>> trendBalance(LocalDate dateStart, LocalDate dateEnd) {
        LocalDate endDate = (dateEnd != null) ? dateEnd : LocalDate.now();

        return list -> {
            double realTrend = list.stream()
                    .filter(t -> isDateInRange(t.getDate(), dateStart, endDate))
                    .mapToDouble(ITransaction::getMoney)
                    .sum();

            double scheduledTrend = calculateScheduledTransactionsForPeriod(null, dateStart, endDate);

            double amortizationTrend = 0;
            if (amortizationManager != null) {
                amortizationTrend = amortizationManager.calculateFutureInstallments(dateStart, endDate);
            }

            double totalTrend = realTrend + scheduledTrend + amortizationTrend;
            return new Pair<>(totalTrend > 0, totalTrend);
        };
    }

    /**
     * Calcola il saldo associato a un tag specifico considerando transazioni reali e programmate.
     * @param type tipo di transazione (SPESA/GUADAGNO)
     * @param tag tag da considerare
     * @return saldo totale per il tag
     */
    @Override
    public double balanceForTag(MovementType type, String tag) {
        double realTransactions = transactionsList.stream()
                .filter(t -> t.getType().equals(type))
                .filter(t -> t.hasTag(tag))
                .mapToDouble(ITransaction::getMoney)
                .sum();

        LocalDate reasonableStart = LocalDate.now().minusYears(10);
        LocalDate reasonableEnd = LocalDate.now().plusYears(10);

        double scheduledTransactions = calculateScheduledTagTransactionsForPeriod(type, tag, reasonableStart, reasonableEnd);

        return realTransactions + scheduledTransactions;
    }

    /**
     * Calcola il saldo in un intervallo di date, considerando anche le rate di ammortamento future.
     * @param type tipo di transazione (SPESA/GUADAGNO), può essere null
     * @param dateStart data di inizio
     * @param dateEnd data di fine
     * @return saldo totale nel periodo
     */
    @Override
    public double balanceForDates(MovementType type, LocalDate dateStart, LocalDate dateEnd) {
        LocalDate endDate = (dateEnd != null) ? dateEnd : LocalDate.now();

        double realTransactions = transactionsList.stream()
                .filter(t -> type == null || t.getType().equals(type))
                .filter(t -> isDateInRange(t.getDate(), dateStart, endDate))
                .mapToDouble(ITransaction::getMoney)
                .sum();

        double scheduledTransactions = calculateScheduledTransactionsForPeriod(type, dateStart, endDate);

        double amortizationTransactions = 0;
        if (amortizationManager != null && type == MovementType.SPESA) {
            amortizationTransactions = amortizationManager.calculateFutureInstallments(dateStart, endDate);
        }

        return realTransactions + scheduledTransactions + amortizationTransactions;
    }

    /**
     * Calcola il saldo per un tag in un intervallo di date.
     * @param type tipo di transazione
     * @param tag tag da considerare
     * @param dateStart data di inizio
     * @param dateEnd data di fine
     * @return saldo totale
     */
    @Override
    public double balanceForTag(MovementType type, String tag, LocalDate dateStart, LocalDate dateEnd) {
        LocalDate endDate = (dateEnd != null) ? dateEnd : LocalDate.now();

        double realTransactions = transactionsList.stream()
                .filter(t -> type == null || t.getType().equals(type))
                .filter(t -> t.hasTag(tag))
                .filter(t -> isDateInRange(t.getDate(), dateStart, endDate))
                .mapToDouble(ITransaction::getMoney)
                .sum();

        double scheduledTransactions = calculateScheduledTagTransactionsForPeriod(type, tag, dateStart, dateEnd);

        return realTransactions + scheduledTransactions;
    }

    /**
     * Restituisce un HashMap con saldo totale per ogni combinazione di tag per un tipo di transazione specifico.
     * @param type tipo di transazione
     * @return mappa <tag, saldo totale>
     */
    @Override
    public HashMap<String, Double> balanceForEachTag(MovementType type) {
        HashMap<String, Double> map = new HashMap<>();

        transactionsList.stream()
                .filter(t -> t.getType().equals(type))
                .filter(t -> !t.getTags().isEmpty())
                .forEach(transaction -> {
                    double amount = transaction.getMoney();
                    String tagKey = transaction.getTags().stream()
                            .map(ITag::getName)
                            .sorted()
                            .collect(Collectors.joining(", "));
                    map.put(tagKey, map.getOrDefault(tagKey, 0.0) + amount);
                });

        return map;
    }

    /**
     * Restituisce la lista delle transazioni reali.
     * @return lista di transazioni
     */
    @Override
    public ArrayList<ITransaction> getList() {
        return new ArrayList<>(transactionsList);
    }

    /**
     * Imposta la lista delle transazioni.
     * @param list lista di transazioni da impostare
     */
    @Override
    public void setList(ArrayList<ITransaction> list) {
        if (list != null) {
            this.transactionsList = new ArrayList<>(list);
        }
    }

    /**
     * Imposta il manager delle transazioni programmate.
     * @param scheduledTransactionManager manager da usare
     */
    @Override
    public void setScheduledTransactionManager(ScheduledTransactionManager scheduledTransactionManager) {
        this.scheduledTransactionManager = scheduledTransactionManager;
    }

    /**
     * Calcola il totale delle transazioni programmate nel periodo specificato.
     * @param type tipo di transazione, può essere null
     * @param start data di inizio
     * @param end data di fine
     * @return totale delle transazioni programmate
     */
    public double calculateScheduledTransactionsForPeriod(MovementType type, LocalDate start, LocalDate end) {
        if (scheduledTransactionManager == null) return 0;

        double total = 0;
        for (ScheduledTransaction scheduled : scheduledTransactionManager.getScheduledTransactions()) {
            if ((type == null || scheduled.getType().equals(type)) && scheduled.isActive()) {
                int occurrences = calculateScheduledOccurrences(scheduled, start, end);
                double amount = scheduled.getAmount();

                if (scheduled.getType() == MovementType.SPESA) {
                    amount = -Math.abs(amount);
                } else if (scheduled.getType() == MovementType.GUADAGNO) {
                    amount = Math.abs(amount);
                }

                total += occurrences * amount;
            }
        }
        return total;
    }

    /**
     * Calcola il totale delle transazioni programmate associate a un tag nel periodo specificato.
     * @param type tipo di transazione
     * @param tag tag della transazione
     * @param start data di inizio
     * @param end data di fine
     * @return totale per il tag
     */
    @Override
    public double calculateScheduledTagTransactionsForPeriod(MovementType type, String tag, LocalDate start, LocalDate end) {
        if (scheduledTransactionManager == null) return 0;

        double total = 0;
        for (ScheduledTransaction scheduled : scheduledTransactionManager.getScheduledTransactions()) {
            if ((type == null || scheduled.getType().equals(type)) &&
                    scheduled.isActive() &&
                    scheduled.hasTag(tag)) {
                total += calculateScheduledOccurrences(scheduled, start, end) * scheduled.getAmount();
            }
        }
        return total;
    }

    // ==================== METODI PRIVATI ====================

    /** Controlla se una data è compresa tra start e end */
    private boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /** Calcola le occorrenze totali in un periodo per una transazione programmata */
    private int calculateScheduledOccurrences(ScheduledTransaction scheduled, LocalDate periodStart, LocalDate periodEnd) {
        if (!scheduled.isActive()) return 0;

        int occurrences = 0;
        LocalDate currentDate = scheduled.getStartDate();

        // Se la transazione non è ancora iniziata nel periodo
        if (currentDate.isAfter(periodEnd)) {
            return 0;
        }

        // Avanza fino all'inizio del periodo
        while (currentDate.isBefore(periodStart)) {
            currentDate = calculateNextDate(currentDate, scheduled.getRecurrence());
            if (currentDate.isAfter(periodEnd) ||
                    (scheduled.getEndDate() != null && currentDate.isAfter(scheduled.getEndDate()))) {
                return 0;
            }
        }

        // Conta le occorrenze nel periodo (anche future)
        int maxIterations = 1000;
        while (!currentDate.isAfter(periodEnd) && occurrences < maxIterations) {
            if (!currentDate.isBefore(periodStart) &&
                    (scheduled.getEndDate() == null || !currentDate.isAfter(scheduled.getEndDate()))) {
                occurrences++;
            }

            currentDate = calculateNextDate(currentDate, scheduled.getRecurrence());
            if (currentDate.isAfter(periodEnd) ||
                    (scheduled.getEndDate() != null && currentDate.isAfter(scheduled.getEndDate()))) {
                break;
            }
        }

        return occurrences;
    }

    /** Calcola la prossima data di una transazione programmata in base alla ricorrenza */
    private LocalDate calculateNextDate(LocalDate currentDate, RecurrenceType recurrence) {
        try {
            return switch (recurrence) {
                case GIORNALIERO -> currentDate.plusDays(1);
                case SETTIMANALE -> currentDate.plusWeeks(1);
                case MENSILE -> currentDate.plusMonths(1);
                case ANNUALE -> currentDate.plusYears(1);
            };
        } catch (Exception e) {
            // In caso di errore (es. data impossibile), ritorna la data corrente
            System.err.println("Errore nel calcolo della prossima data: " + e.getMessage());
            return currentDate;
        }
    }

    /** Predicato per filtrare transazioni in un intervallo di date e tipo */
    private Predicate<ITransaction> forDates(MovementType type, LocalDate... dates) {
        if (type != null) {
            return t -> isDateInRange(t.getDate(), dates[0], dates[1]) && t.getType().equals(type);
        }
        return t -> isDateInRange(t.getDate(), dates[0], dates[1]);
    }
}
