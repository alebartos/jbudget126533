package it.unicam.cs.mpgc.jbudget126533.util;

/**
 * Classe generica che rappresenta una coppia di valori di due tipi diversi.
 * Dichiarata `final` per impedire l'ereditarietà.
 *
 * @param <T> tipo del primo elemento della coppia
 * @param <S> tipo del secondo elemento della coppia
 */
public final class Pair<T, S> {

    private T first;
    private S second;

    /**
     * Costruttore che inizializza entrambi gli elementi della coppia.
     *
     * @param first primo elemento
     * @param second secondo elemento
     */
    public Pair(T first, S second) {
        this.setFirst(first);
        this.setSecond(second);
    }

    /** @return il primo elemento della coppia */
    public T getFirst() {
        return first;
    }

    /** Setter privato per garantire l'immuabilità della coppia dall'esterno */
    private void setFirst(T first) {
        this.first = first;
    }

    /** @return il secondo elemento della coppia */
    public S getSecond() {
        return second;
    }

    /** Setter privato per garantire l'immuabilità della coppia dall'esterno */
    private void setSecond(S second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return first + ", " + second;
    }
}
