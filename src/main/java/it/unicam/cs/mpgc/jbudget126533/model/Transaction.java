package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementazione concreta di una singola transazione.
 * Estende la classe astratta Movement.
 */
public class Transaction extends Movement {

    /** Costruttore vuoto  */
    public Transaction() {
        super();
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
        super(type, person, money, date, tags);
        this.type = type;
        this.person = person;
        this.user = person != null ? person.getName() : "";
        this.money = money;
        this.date = date;
        this.tags = new ArrayList<>(tags);
    }

    // ----------------- UTILITY -----------------

    /**
     * Restituisce una rappresentazione testuale leggibile della transazione.
     */
    @Override
    public String toString() {
        String tagsString = getTags().stream()
                .map(ITag::getName)
                .collect(Collectors.joining(", "));

        return getType() + spaceWord(getType()) + "| " +
                getUser() + spaceWord(getUser()) + "| " +
                getMoney() + spaceWord(getMoney()) + "| " +
                getDate() + spaceWord(getDate()) + "| " +
                tagsString;
    }
}
