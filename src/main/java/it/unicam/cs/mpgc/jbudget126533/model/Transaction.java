package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione concreta di una singola transazione.
 * Supporta utenti, importi, data, tipo di movimento e tag multipli.
 */
public class Transaction implements ITransaction {

    private MovementType type;
    private String user;
    private double money;
    private LocalDate date;
    private List<ITag> tags;
    private Person person;

    /** Costruttore vuoto (utile per Gson o serializzazione/deserializzazione) */
    public Transaction() {
        this.tags = new ArrayList<>();
    }

    /**
     * Costruttore con lista di tag multipli.
     *
     * @param type Tipo di movimento
     * @param person Nome utente
     * @param money Importo
     * @param date Data della transazione
     * @param tags Lista di tag
     */
    public Transaction(MovementType type, Person person, double money, LocalDate date, List<ITag> tags) {
        this.type = type;
        this.person = person;
        this.user = person != null ? person.getName() : "";
        this.money = money;
        this.date = date;
        this.tags = new ArrayList<>(tags);
    }

    // ----------------- GETTER e SETTER -----------------

    @Override
    public MovementType getType() { return type; }
    @Override
    public void setType(MovementType type) { this.type = type; }

    @Override
    public String getUser() { return user; }
    @Override
    public void setUser(String user) { this.user = user; }

    @Override
    public double getMoney() { return money; }
    @Override
    public void setMoney(double money) { this.money = money; }

    @Override
    public LocalDate getDate() { return date; }
    @Override
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public List<ITag> getTags() { return new ArrayList<>(tags); }
    @Override
    public void setTags(List<ITag> tags) { this.tags = new ArrayList<>(tags); }

    public Person getPerson() { return person; }
    public void setPerson(Person person) {
        this.person = person;
        this.user = person != null ? person.getName() : "";
    }

    @Override
    public void addTag(ITag tag) { if (tag != null) tags.add(tag); }
    @Override
    public void removeTag(ITag tag) { tags.remove(tag); }

    @Override
    public boolean hasTag(ITag tag) { return tags.contains(tag); }

    @Override
    public boolean hasTag(String tagName) {
        return tags.stream().anyMatch(tag -> tag.getName().equalsIgnoreCase(tagName));
    }

    // ----------------- UTILITY -----------------

    /**
     * Restituisce una rappresentazione testuale leggibile della transazione.
     */
    @Override
    public String toString() {
        String tagsString = tags.stream()
                .map(ITag::getName)
                .collect(Collectors.joining(", "));

        return type + spaceWord(type) + "| " +
                user + spaceWord(user) + "| " +
                money + spaceWord(money) + "| " +
                date + spaceWord(date) + "| " +
                tagsString;
    }
}
