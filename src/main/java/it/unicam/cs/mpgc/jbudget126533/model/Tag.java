package it.unicam.cs.mpgc.jbudget126533.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementazione concreta di un tag gerarchico.
 * <p>
 * Ogni tag può avere un parent (genitore) e una lista di figli, formando
 * una struttura ad albero. I tag possono essere utilizzati per categorizzare
 * transazioni o altre entità nell'applicazione.
 * </p>
 */
public class Tag implements ITag {

    private String name;
    private ITag parent;
    private final List<ITag> children;
    private String color;
    private String description;

    /**
     * Costruttore vuoto per Gson.
     * Inizializza la lista dei figli e i valori di default.
     */
    public Tag() {
        this.children = new ArrayList<>();
        this.color = "#000000";
        this.description = "";
    }

    /**
     * Crea un tag con un nome specificato.
     *
     * @param name Nome del tag
     */
    public Tag(String name) {
        this();
        this.name = name;
    }

    /**
     * Crea un tag con un nome e un parent specificati.
     *
     * @param name   Nome del tag
     * @param parent Tag genitore
     */
    public Tag(String name, ITag parent) {
        this(name);
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    @Override
    public String getName() { return name; }

    @Override
    public void setName(String name) { this.name = name; }

    @Override
    public ITag getParent() { return parent; }

    /**
     * Imposta il tag genitore.
     * <p>
     * Rimuove automaticamente il tag dal vecchio parent e lo aggiunge al nuovo,
     * evitando loop infiniti.
     * </p>
     *
     * @param parent Nuovo tag genitore
     */
    @Override
    public void setParent(ITag parent) {
        if (this.parent == parent) {
            return;
        }

        if (this.parent != null) {
            this.parent.removeChild(this);
        }

        this.parent = parent;

        if (parent != null && !parent.getChildren().contains(this)) {
            parent.getChildren().add(this);
        }
    }

    @Override
    public List<ITag> getChildren() { return new ArrayList<>(children); }

    @Override
    public void addChild(ITag child) {
        if (!children.contains(child)) {
            children.add(child);
            if (child.getParent() != this) {
                child.setParent(this);
            }
        }
    }

    @Override
    public void removeChild(ITag child) {
        if (children.contains(child)) {
            children.remove(child);
            if (child.getParent() == this) {
                child.setParent(null);
            }
        }
    }

    @Override
    public boolean hasChildren() { return !children.isEmpty(); }

    @Override
    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    @Override
    public boolean isDescendantOf(ITag potentialAncestor) {
        if (potentialAncestor == null) return false;
        ITag current = parent;
        while (current != null) {
            if (current.equals(potentialAncestor)) return true;
            current = current.getParent();
        }
        return false;
    }

    @Override
    public boolean isAncestorOf(ITag potentialDescendant) {
        return potentialDescendant != null && potentialDescendant.isDescendantOf(this);
    }

    @Override
    public String getColor() { return color; }

    @Override
    public void setColor(String color) { this.color = color; }

    @Override
    public String getDescription() { return description; }

    @Override
    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name) &&
                Objects.equals(parent, tag.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    @Override
    public String toString() {
        return getFullPath();
    }

}
