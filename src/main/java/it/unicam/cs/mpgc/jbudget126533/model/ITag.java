package it.unicam.cs.mpgc.jbudget126533.model;

import java.util.List;

/**
 * Interfaccia che rappresenta un <b>tag gerarchico</b> utilizzato
 * per categorizzare le transazioni.
 * <p>
 * Un tag può avere un tag padre e uno o più tag figli, permettendo
 * di costruire una struttura ad albero (gerarchia di categorie).
 * Inoltre, ogni tag può avere un colore e una descrizione associata.
 * </p>
 */
public interface ITag {

    // ==================== NOME ====================

    /**
     * Restituisce il nome del tag.
     *
     * @return nome del tag
     */
    String getName();

    /**
     * Imposta il nome del tag.
     *
     * @param name nuovo nome del tag
     */
    void setName(String name);

    // ==================== GERARCHIA ====================

    /**
     * Restituisce il tag padre (se esiste).
     *
     * @return tag padre o {@code null} se il tag è radice
     */
    ITag getParent();

    /**
     * Imposta il tag padre.
     *
     * @param parent nuovo tag padre
     */
    void setParent(ITag parent);

    /**
     * Restituisce la lista dei tag figli.
     *
     * @return lista dei figli di questo tag
     */
    List<ITag> getChildren();

    /**
     * Aggiunge un tag figlio.
     *
     * @param child tag da aggiungere come figlio
     */
    void addChild(ITag child);

    /**
     * Rimuove un tag figlio.
     *
     * @param child tag da rimuovere dai figli
     */
    void removeChild(ITag child);

    /**
     * Verifica se il tag ha figli.
     *
     * @return {@code true} se il tag ha almeno un figlio, {@code false} altrimenti
     */
    boolean hasChildren();

    // ==================== RELAZIONI GERARCHICHE ====================

    /**
     * Restituisce il percorso completo del tag nella gerarchia,
     * ad esempio: {@code "Spese/Alimentari/Supermercato"}.
     *
     * @return percorso completo del tag
     */
    String getFullPath();

    /**
     * Verifica se il tag corrente è un discendente di un altro tag.
     *
     * @param potentialAncestor possibile tag antenato
     * @return {@code true} se il tag corrente discende da quello specificato
     */
    boolean isDescendantOf(ITag potentialAncestor);

    /**
     * Verifica se il tag corrente è un antenato di un altro tag.
     *
     * @param potentialDescendant possibile tag discendente
     * @return {@code true} se il tag corrente è un antenato di quello specificato
     */
    boolean isAncestorOf(ITag potentialDescendant);

    // ==================== ATTRIBUTI VISIVI ====================

    /**
     * Restituisce il colore associato al tag (es. in formato HEX).
     *
     * @return colore del tag
     */
    String getColor();

    /**
     * Imposta il colore associato al tag.
     *
     * @param color colore in formato stringa (es. HEX)
     */
    void setColor(String color);

    // ==================== DESCRIZIONE ====================

    /**
     * Restituisce la descrizione testuale del tag.
     *
     * @return descrizione del tag
     */
    String getDescription();

    /**
     * Imposta la descrizione del tag.
     *
     * @param description nuova descrizione del tag
     */
    void setDescription(String description);
}