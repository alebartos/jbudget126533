package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un piano di ammortamento francese a rate costanti.
 * <p>
 * Ogni rata è composta da una quota capitale e una quota interessi,
 * calcolate in base al tasso di interesse annuo e al numero totale di rate.
 * Le rate vengono generate automaticamente alla creazione del piano.
 * </p>
 * <p>
 * Le transazioni reali vengono create solo al momento della scadenza di ciascuna rata,
 * tramite {@link AmortizationManager}.
 * </p>
 */
public class AmortizationPlan {
    private String id;
    private String description;
    private double principalAmount;
    private double annualInterestRate;
    private int numberOfInstallments;
    private LocalDate startDate;
    private List<ITag> tags;
    private List<Installment> installments;

    /**
     * Costruttore vuoto richiesto per Gson (deserializzazione).
     * Inizializza liste vuote.
     */
    public AmortizationPlan() {
        this.tags = new ArrayList<>();
        this.installments = new ArrayList<>();
    }

    /**
     * Costruisce un piano di ammortamento francese a rate costanti.
     * <p>
     * Genera automaticamente le rate in base ai parametri forniti.
     * </p>
     *
     * @param id                  identificativo univoco del piano
     * @param description         descrizione testuale del piano
     * @param principalAmount     importo del capitale iniziale
     * @param annualInterestRate  tasso di interesse annuo (in percentuale)
     * @param numberOfInstallments numero di rate totali
     * @param startDate           data di inizio del piano
     * @param tags                lista di tag associati al piano
     */
    public AmortizationPlan(String id, String description, double principalAmount, double annualInterestRate,
                            int numberOfInstallments, LocalDate startDate, List<ITag> tags) {
        this.id = id;
        this.description = description;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.numberOfInstallments = numberOfInstallments;
        this.startDate = startDate;
        this.tags = new ArrayList<>(tags);
        this.installments = new ArrayList<>();
        generateInstallments();
    }

    /**
     * Genera tutte le rate (installments) del piano usando il metodo francese.
     * Ogni rata ha lo stesso importo complessivo ma quota capitale e interessi variabili.
     */
    private void generateInstallments() {
        double monthlyRate = annualInterestRate / 12 / 100;
        double monthlyPayment = calculateMonthlyPayment(principalAmount, monthlyRate, numberOfInstallments);
        double remainingBalance = principalAmount;

        for (int i = 1; i <= numberOfInstallments; i++) {
            double interest = remainingBalance * monthlyRate;
            double principal = monthlyPayment - interest;
            remainingBalance -= principal;

            Installment installment = new Installment(
                    i,
                    startDate.plusMonths(i - 1),
                    principal,
                    interest,
                    monthlyPayment,
                    Math.max(0, remainingBalance),
                    false,
                    id
            );
            installments.add(installment);
        }
    }

    /**
     * Calcola l'importo di una rata costante con il metodo francese.
     *
     * @param principal   capitale iniziale
     * @param monthlyRate tasso di interesse mensile
     * @param periods     numero totale di rate
     * @return importo della rata
     */
    private double calculateMonthlyPayment(double principal, double monthlyRate, int periods) {
        if (monthlyRate == 0) return principal / periods;
        return principal * (monthlyRate * Math.pow(1 + monthlyRate, periods))
                / (Math.pow(1 + monthlyRate, periods) - 1);
    }

    // ===================== GETTERS =====================

    /** @return identificativo del piano */
    public String getId() { return id; }

    /** @return descrizione del piano */
    public String getDescription() { return description; }

    /** @return capitale iniziale */
    public double getPrincipalAmount() { return principalAmount; }

    /** @return tasso di interesse annuo (percentuale) */
    public double getAnnualInterestRate() { return annualInterestRate; }

    /** @return numero di rate totali */
    public int getNumberOfInstallments() { return numberOfInstallments; }

    /** @return data di inizio del piano */
    public LocalDate getStartDate() { return startDate; }

    /** @return lista dei tag associati (nuova lista indipendente) */
    public List<ITag> getTags() { return new ArrayList<>(tags); }

    /** @return lista delle rate del piano (nuova lista indipendente) */
    public List<Installment> getInstallments() { return new ArrayList<>(installments); }

    /** @return totale degli interessi da pagare sull’intero piano */
    public double getTotalInterest() {
        return installments.stream().mapToDouble(Installment::getInterestAmount).sum();
    }

    /** @return totale complessivo da pagare (capitale + interessi) */
    public double getTotalPayment() {
        return installments.stream().mapToDouble(Installment::getTotalAmount).sum();
    }

    // ===================== SETTERS (necessari per Gson) =====================

    public void setId(String id) { this.id = id; }
    public void setDescription(String description) { this.description = description; }
    public void setPrincipalAmount(double principalAmount) { this.principalAmount = principalAmount; }
    public void setAnnualInterestRate(double annualInterestRate) { this.annualInterestRate = annualInterestRate; }
    public void setNumberOfInstallments(int numberOfInstallments) { this.numberOfInstallments = numberOfInstallments; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setTags(List<ITag> tags) { this.tags = new ArrayList<>(tags); }
    public void setInstallments(List<Installment> installments) { this.installments = new ArrayList<>(installments); }
}
