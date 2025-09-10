package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta base per tutti i movimenti
 * Fornisce implementazioni di base per le proprietà comuni a tutte le transazioni.
 */
public abstract class Movement implements ITransaction {
    protected MovementType type;
    protected String user;
    protected double money;
    protected LocalDate date;
    protected List<ITag> tags;
    protected Person person;

    public Movement() {
        this.tags = new ArrayList<>();
    }

    public Movement(MovementType type, Person person, double money, LocalDate date, List<ITag> tags) {
        this.type = type;
        this.person = person;
        this.user = person != null ? person.getName() : "";
        this.money = money;
        this.date = date;
        this.tags = new ArrayList<>(tags);
    }

    // Implementazioni dei metodi dell'interfaccia ITransaction
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

    @Override
    public <T> String spaceWord(T obj) {
        return " ".repeat(Math.max(0, 15 - obj.toString().length() + 1));
    }
}
