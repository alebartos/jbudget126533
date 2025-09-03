package it.unicam.cs.mpgc.jbudget126533.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Classe principale per la GUI dell'applicazione JBudget basata su JavaFX.
 * <p>
 * Questa classe estende {@link javafx.application.Application} e rappresenta
 * il punto di ingresso dell'interfaccia grafica. Carica il file FXML
 * {@code Menu.fxml} che definisce la struttura della GUI.
 * <p>
 * Implementa l'interfaccia {@link View} per conformarsi al design dell'applicazione.
 */
public class GUIView extends Application implements View {

    /**
     * Punto di ingresso della GUI.
     * <p>
     * Carica il file FXML {@code Menu.fxml}, imposta titolo, dimensioni della finestra
     * e proprietà di ridimensionamento, quindi mostra lo stage principale.
     *
     * @param primaryStage lo stage principale fornito da JavaFX
     * @throws Exception se il caricamento del file FXML fallisce
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Menu.fxml")));
        primaryStage.setTitle("JBudget");
        primaryStage.setScene(new Scene(root, 1100, 750));
        primaryStage.resizableProperty().setValue(true);
        primaryStage.show();
    }

    /**
     * Metodo aperto dall'interfaccia {@link View}.
     * <p>
     * Per la GUI JavaFX, l'apertura è gestita dal metodo {@link #start(Stage)},
     * quindi questo metodo non fa nulla.
     */
    @Override
    public void open() {
        // Metodo non utilizzato per GUI JavaFX
    }

    /**
     * Metodo aperto dall'interfaccia {@link View}.
     * <p>
     * Per la GUI JavaFX, la chiusura viene gestita dallo stage,
     * quindi questo metodo non fa nulla.
     */
    @Override
    public void close() {
        // Metodo non utilizzato per GUI JavaFX
    }

}
