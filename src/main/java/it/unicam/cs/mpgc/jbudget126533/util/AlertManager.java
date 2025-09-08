package it.unicam.cs.mpgc.jbudget126533.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;

/**
 * Classe utility per la gestione centralizzata degli alert.
 * Fornisce metodi statici per mostrare diversi tipi di alert.
 */
public class AlertManager {

    /**
     * Mostra un alert di errore.
     *
     * @param title   titolo della finestra
     * @param message messaggio da mostrare
     */
    public static void showErrorAlert(String title, String message) {
        showAlert(AlertType.ERROR, title, null, message);
    }

    /**
     * Mostra un alert di errore con titolo predefinito.
     *
     * @param message messaggio da mostrare
     */
    public static void showErrorAlert(String message) {
        showErrorAlert("Errore", message);
    }

    /**
     * Mostra un alert di informazione.
     *
     * @param title   titolo della finestra
     * @param message messaggio da mostrare
     */
    public static void showInfoAlert(String title, String message) {
        showAlert(AlertType.INFORMATION, title, null, message);
    }

    /**
     * Mostra un alert di informazione con titolo predefinito.
     *
     * @param message messaggio da mostrare
     */
    public static void showInfoAlert(String message) {
        showInfoAlert("Informazione", message);
    }

    /**
     * Mostra un alert di avviso.
     *
     * @param title   titolo della finestra
     * @param message messaggio da mostrare
     */
    public static void showWarningAlert(String title, String message) {
        showAlert(AlertType.WARNING, title, null, message);
    }

    /**
     * Mostra un alert di avviso con titolo predefinito.
     *
     * @param message messaggio da mostrare
     */
    public static void showWarningAlert(String message) {
        showWarningAlert("Avviso", message);
    }

    /**
     * Mostra un alert di conferma.
     *
     * @param title   titolo della finestra
     * @param header  header dell'alert
     * @param message messaggio da mostrare
     * @return true se l'utente ha cliccato OK, false altrimenti
     */
    public static boolean showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Mostra un alert di conferma con titolo predefinito.
     *
     * @param message messaggio da mostrare
     * @return true se l'utente ha cliccato OK, false altrimenti
     */
    public static boolean showConfirmationAlert(String message) {
        return showConfirmationAlert("Conferma", null, message);
    }

    /**
     * Metodo generico per mostrare alert.
     *
     * @param type    tipo di alert
     * @param title   titolo della finestra
     * @param header  header dell'alert (può essere null)
     * @param message messaggio da mostrare
     */
    private static void showAlert(AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}