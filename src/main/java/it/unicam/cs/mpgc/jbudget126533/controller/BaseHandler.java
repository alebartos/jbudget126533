package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.util.AlertManager;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Classe astratta base per tutti gli handler che gestiscono operazioni CRUD su entità.
 * Fornisce metodi utility comuni per la gestione degli alert, selezione e operazioni di base.
 *
 * @param <T> il tipo di entità gestita dall'handler
 */
public abstract class BaseHandler<T> {

    protected final Ledger ledger;

    protected BaseHandler(Ledger ledger) {
        this.ledger = ledger;
    }

    /**
     * Mostra un alert di conferma per operazioni critiche come l'eliminazione.
     *
     * @param title titolo dell'alert
     * @param header testo dell'header
     * @param content contenuto dell'alert
     * @return true se l'utente ha confermato, false altrimenti
     */
    protected boolean showConfirmationAlert(String title, String header, String content) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle(title);
        confirmation.setHeaderText(header);
        confirmation.setContentText(content);

        Optional<ButtonType> result = confirmation.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Esegue un'operazione su un elemento selezionato in una tabella.
     *
     * @param table la tabella da cui ottenere l'elemento selezionato
     * @param onSelected azione da eseguire sull'elemento selezionato
     * @param noSelectionMessage messaggio da mostrare se nessun elemento è selezionato
     * @return true se un elemento era selezionato e l'operazione è stata eseguita, false altrimenti
     */
    protected boolean executeOnSelectedItem(TableView<T> table, Consumer<T> onSelected, String noSelectionMessage) {
        T selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            onSelected.accept(selected);
            return true;
        } else {
            if (noSelectionMessage != null && !noSelectionMessage.isEmpty()) {
                AlertManager.showWarningAlert(noSelectionMessage);
            }
            return false;
        }
    }

    /**
     * Aggiorna una tabella con i dati forniti.
     *
     * @param table la tabella da aggiornare
     * @param items la lista di elementi da visualizzare
     */
    protected void refreshTable(TableView<T> table, List<T> items) {
        if (table != null) {
            ObservableList<T> observableList = table.getItems();
            observableList.clear();
            observableList.addAll(items);
            table.refresh();
        }
    }

    /**
     * Metodo astratto per l'aggiornamento della tabella principale dell'handler.
     * Ogni handler concreto deve implementare questo metodo.
     */
    public abstract void refreshTable();

    /**
     * Metodo astratto per la pulizia dei campi di input.
     * Ogni handler concreto deve implementare questo metodo.
     */
    protected abstract void clearInputFields();
}