package it.unicam.cs.mpgc.jbudget126533.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaccia che definisce il contratto per una transazione nel sistema di gestione del budget.
 * <p>
 * Una transazione rappresenta un movimento finanziario (entrata o uscita) associato ad un utente,
 * a uno o più {@link ITag} e caratterizzato da una data, un importo e un {@link MovementType}.
 * </p>
 */
public interface ITransaction {

    /**
     * Restituisce l'utente associato alla transazione.
     *
     * @return nome dell'utente
     */
    String getUser();

    /**
     * Imposta l'utente associato alla transazione.
     *
     * @param user nome dell'utente
     */
    void setUser(String user);

    /**
     * Restituisce la lista di tag associati alla transazione.
     * I tag servono per categorizzare le spese o i guadagni.
     *
     * @return lista di {@link ITag}
     */
    List<ITag> getTags();

    /**
     * Imposta i tag associati alla transazione.
     *
     * @param tags lista di {@link ITag} da associare
     */
    void setTags(List<ITag> tags);

    /**
     * Aggiunge un singolo tag alla transazione.
     *
     * @param tag il tag da aggiungere
     */
    void addTag(ITag tag);

    /**
     * Rimuove un singolo tag dalla transazione.
     *
     * @param tag il tag da rimuovere
     */
    void removeTag(ITag tag);

    /**
     * Verifica se la transazione contiene un determinato tag.
     *
     * @param tag tag da controllare
     * @return {@code true} se il tag è presente, {@code false} altrimenti
     */
    boolean hasTag(ITag tag);

    /**
     * Verifica se la transazione contiene un tag con un dato nome.
     *
     * @param tagName nome del tag
     * @return {@code true} se il tag è presente, {@code false} altrimenti
     */
    boolean hasTag(String tagName);

    /**
     * Restituisce l'importo della transazione.
     * <ul>
     *   <li>Valore positivo per guadagni.</li>
     *   <li>Valore negativo per spese.</li>
     * </ul>
     *
     * @return importo della transazione
     */
    double getMoney();

    /**
     * Imposta l'importo della transazione.
     *
     * @param money importo (positivo per guadagni, negativo per spese)
     */
    void setMoney(double money);

    /**
     * Restituisce la data in cui è avvenuta la transazione.
     *
     * @return data della transazione
     */
    LocalDate getDate();

    /**
     * Imposta la data della transazione.
     *
     * @param date data della transazione
     */
    void setDate(LocalDate date);

    /**
     * Restituisce il tipo di transazione (entrata o uscita).
     *
     * @return tipo di movimento {@link MovementType}
     */
    MovementType getType();

    /**
     * Imposta il tipo di movimento della transazione.
     *
     * @param type tipo di movimento {@link MovementType}
     */
    void setType(MovementType type);

    /**
     * Restituisce una stringa di spazi per formattare in modo uniforme
     * i campi della transazione durante la visualizzazione testuale.
     * <p>
     * Questo metodo è di supporto per generare output tabellari allineati.
     * </p>
     *
     * @param obj oggetto da valutare (usualmente un campo della transazione)
     * @param <T> tipo dell'oggetto
     * @return stringa contenente spazi di padding
     */
    default <T> String spaceWord(T obj) {
        return " ".repeat(Math.max(0, 15 - obj.toString().length() + 1));
    }
}
