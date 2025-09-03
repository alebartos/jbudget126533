package it.unicam.cs.mpgc.jbudget126533;

import it.unicam.cs.mpgc.jbudget126533.controller.Ledger;
import it.unicam.cs.mpgc.jbudget126533.model.BudgetManagement;
import it.unicam.cs.mpgc.jbudget126533.util.FileSystemUtils;
import it.unicam.cs.mpgc.jbudget126533.view.ConsoleView;
import it.unicam.cs.mpgc.jbudget126533.view.GUIView;
import it.unicam.cs.mpgc.jbudget126533.view.View;
import javafx.application.Application;

/**
 * Classe principale dell'applicazione JBudget.
 * <p>
 * Si occupa di avviare l'applicazione sia in modalità Console sia in modalità GUI
 * a seconda degli argomenti passati da riga di comando.
 */
public class App {

    /** Vista dell'applicazione (Console o GUI). */
    private final View view;

    /**
     * Costruttore che inizializza l'app con la vista specificata.
     *
     * @param view la vista dell'applicazione da utilizzare
     */
    public App(View view) {
        this.view = view;
    }

    /**
     * Punto di ingresso dell'applicazione.
     * <p>
     * Se non vengono passati argomenti, lancia la GUI. Altrimenti, lancia la modalità Console.
     *
     * @param args argomenti da riga di comando
     */
    public static void main(String[] args){
        try {
            // Inizializza la struttura delle directory
            FileSystemUtils.initializeDataDirectory();

            // Crea file mancanti
            FileSystemUtils.createMissingFiles();
            FileSystemUtils.checkAllFilesExist();

            // Crea backup
            FileSystemUtils.createBackup();

            if (args.length == 0) {
                new App(new ConsoleView(new Ledger(new BudgetManagement()))).startGUI();
            } else {
                new App(new ConsoleView(new Ledger(new BudgetManagement()))).startConsole();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Avvia l'applicazione in modalità Console.
     *
     */
    private void startConsole() {
        view.open();
    }

    /**
     * Avvia l'applicazione in modalità GUI.
     * <p>
     * Utilizza JavaFX per caricare la classe {@link GUIView}.
     */
    private void startGUI() {
        Application.launch(GUIView.class);
    }
}
