package it.unicam.cs.mpgc.jbudget126533.controller;

import it.unicam.cs.mpgc.jbudget126533.model.Person;
import it.unicam.cs.mpgc.jbudget126533.model.PersonManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Gestisce l'interazione con l'interfaccia utente per la gestione delle persone.
 * Fornisce metodi per inizializzare la tabella delle persone, aggiungere nuove persone,
 * eliminare persone esistenti, e aggiornare la visualizzazione.
 */
public class PersonHandler {
    private final TableView<Person> personTable;
    private final TextField personNameField;
    private final TextField personEmailField;
    private final TextField personPhoneField;
    private Runnable onPersonListChanged;

    /**
     * Costruttore che inizializza i riferimenti ai componenti UI coinvolti nella gestione delle persone.
     *
     * @param personTable la tabella che visualizza la lista delle persone
     * @param personNameField campo di testo per il nome della persona
     * @param personEmailField campo di testo per l'email della persona
     * @param personPhoneField campo di testo per il telefono della persona
     */
    public PersonHandler(TableView<Person> personTable, TextField personNameField,
                         TextField personEmailField, TextField personPhoneField) {
        this.personTable = personTable;
        this.personNameField = personNameField;
        this.personEmailField = personEmailField;
        this.personPhoneField = personPhoneField;
    }

    /**
     * Inizializza la gestione della persona configurando la tabella e caricando i dati.
     */
    public void initializePersonManagement() {
        // Configura la tabella delle persone
        configurePersonTable();

        // Carica le persone
        loadPersonsTable();
    }

    /**
     * Configura le colonne della tabella delle persone, incluso l'inserimento di un pulsante "Elimina"
     * per ogni riga della tabella.
     */
    private void configurePersonTable() {
        if (personTable.getColumns().size() > 0) {
            TableColumn<Person, String> nameColumn = (TableColumn<Person, String>) personTable.getColumns().get(0);
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        }

        if (personTable.getColumns().size() > 1) {
            TableColumn<Person, String> emailColumn = (TableColumn<Person, String>) personTable.getColumns().get(1);
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        }

        if (personTable.getColumns().size() > 2) {
            TableColumn<Person, String> phoneColumn = (TableColumn<Person, String>) personTable.getColumns().get(2);
            phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        }

        // Colonna azioni con bottone Elimina
        if (personTable.getColumns().size() > 3) {
            TableColumn<Person, Void> actionsColumn = (TableColumn<Person, Void>) personTable.getColumns().get(3);
            actionsColumn.setCellFactory(param -> new TableCell<Person, Void>() {
                private final Button deleteButton = new Button("Elimina");

                {
                    deleteButton.setOnAction(event -> {
                        Person person = getTableView().getItems().get(getIndex());
                        deletePerson(person);
                    });
                    deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            });
        }
    }

    /**
     * Imposta la callback da eseguire quando la lista delle persone viene modificata.
     *
     * @param callback una Runnable da eseguire al cambiamento della lista
     */
    public void setOnPersonListChanged(Runnable callback) {
        this.onPersonListChanged = callback;
    }

    /**
     * Aggiunge una nuova persona basandosi sui dati inseriti nei campi di testo.
     * Se il nome è vuoto o già presente, mostra un messaggio di errore.
     * Aggiorna la tabella e pulisce i campi di input dopo l'aggiunta.
     *
     * @param event l'evento generato dall'azione utente (es. click sul pulsante Aggiungi)
     */
    public void addPerson(ActionEvent event) {
        String name = personNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Inserisci un nome!");
            return;
        }

        if (PersonManager.getPerson(name) != null) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Una persona con questo nome esiste già!");
            return;
        }

        Person person = new Person(name,
                personEmailField.getText().trim(),
                personPhoneField.getText().trim());

        PersonManager.addPerson(person);
        loadPersonsTable();
        clearPersonFields();


        if (onPersonListChanged != null) {
            onPersonListChanged.run();
        }
    }

    /**
     * Elimina una persona dalla lista dopo conferma da parte dell'utente.
     * Aggiorna la tabella e mostra un messaggio di successo o errore.
     *
     * @param person la persona da eliminare
     */
    private void deletePerson(Person person) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Conferma eliminazione");
        confirmation.setHeaderText("Eliminare la persona '" + person.getName() + "'?");
        confirmation.setContentText("Questa operazione non può essere annullata.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean removed = PersonManager.removePerson(person.getName());
                if (removed) {
                    loadPersonsTable();

                    if (onPersonListChanged != null) {
                        onPersonListChanged.run();
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Successo", "Persona eliminata con successo!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Errore", "Impossibile eliminare la persona!");
                }
            }
        });
    }

    /**
     * Carica la lista aggiornata delle persone nella tabella.
     */
    private void loadPersonsTable() {
        personTable.setItems(FXCollections.observableArrayList(PersonManager.getAllPersons()));
    }

    /**
     * Pulisce i campi di input per nome, email e telefono della persona.
     */
    private void clearPersonFields() {
        personNameField.clear();
        personEmailField.clear();
        personPhoneField.clear();
    }

    /**
     * Mostra una finestra di alert/modal con il tipo specificato, titolo e messaggio.
     *
     * @param type il tipo di alert (es. errore, informazione, conferma)
     * @param title il titolo della finestra di alert
     * @param message il messaggio da mostrare all'utente
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}