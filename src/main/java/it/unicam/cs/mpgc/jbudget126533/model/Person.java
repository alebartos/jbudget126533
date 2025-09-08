package it.unicam.cs.mpgc.jbudget126533.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta una persona con nome, email, telefono e una lista di tag predefiniti.
 * Fornisce metodi per accedere e modificare le informazioni della persona e gestire i tag.
 */
public class Person {
    private String name;
    private String email;
    private String phone;
    private List<ITag> defaultTags;

    /**
     * Costruttore che crea una persona con solo il nome.
     * Inizializza la lista dei tag predefiniti vuota.
     *
     * @param name il nome della persona
     */
    public Person(String name) {
        this.name = name;
        this.defaultTags = new ArrayList<>();
    }

    /**
     * Costruttore che crea una persona con nome, email e telefono.
     * Inizializza la lista dei tag predefiniti vuota.
     *
     * @param name il nome della persona
     * @param email l'email della persona
     * @param phone il numero di telefono della persona
     */
    public Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.defaultTags = new ArrayList<>();
    }

    /**
     * Restituisce il nome della persona.
     *
     * @return il nome
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce l'email della persona.
     *
     * @return l'email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Restituisce il numero di telefono della persona.
     *
     * @return il telefono
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Restituisce una copia della lista dei tag predefiniti della persona.
     * Questo previene modifiche dirette alla lista interna.
     *
     * @return lista dei tag predefiniti
     */
    public List<ITag> getDefaultTags() {
        return new ArrayList<>(defaultTags);
    }

    /**
     * Imposta la lista dei tag predefiniti della persona con una copia della lista fornita.
     *
     * @param defaultTags la nuova lista di tag da impostare
     */
    public void setDefaultTags(List<ITag> defaultTags) {
        this.defaultTags = new ArrayList<>(defaultTags);
    }

    /**
     * Aggiunge un tag predefinito alla lista della persona.
     *
     * @param tag il tag da aggiungere
     */
    public void addDefaultTag(ITag tag) {
        defaultTags.add(tag);
    }

    /**
     * Rimuove un tag predefinito dalla lista della persona.
     *
     * @param tag il tag da rimuovere
     */
    public void removeDefaultTag(ITag tag) {
        defaultTags.remove(tag);
    }

    /**
     * Restituisce la rappresentazione testuale della persona, basata sul nome.
     *
     * @return il nome della persona
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Determina l'uguaglianza tra due persone basandosi sul nome ignorando la differenza tra maiuscole e minuscole.
     *
     * @param obj l'oggetto da confrontare
     * @return true se i nomi sono uguali (ignorando il case), false altrimenti
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return name.equalsIgnoreCase(person.name);
    }

    /**
     * Calcola l'hashcode della persona basato sul nome convertito in minuscolo.
     *
     * @return l'hashcode
     */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
