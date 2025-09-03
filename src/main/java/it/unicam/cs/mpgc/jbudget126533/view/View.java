package it.unicam.cs.mpgc.jbudget126533.view;

/**
 * Interfaccia che rappresenta una vista generica dell'applicazione.
 * <p>
 * Fornisce i metodi fondamentali per aprire e chiudere l'interfaccia utente.
 * Può essere implementata sia da una GUI (JavaFX) che da una Console.
 */
public interface View {

    /**
     * Metodo per avviare/aprire la vista.
     * <p>
     * Per le implementazioni console, può gestire input dell'utente e stampare output.
     * Per le implementazioni GUI, viene tipicamente invocato all'avvio dell'applicazione.
     *
     */
    void open();

    /**
     * Metodo per chiudere la vista.
     * <p>
     * Per le implementazioni console, può terminare il loop principale.
     * Per le implementazioni GUI, può chiudere la finestra principale o eseguire pulizie necessarie.
     */
    void close();
}
