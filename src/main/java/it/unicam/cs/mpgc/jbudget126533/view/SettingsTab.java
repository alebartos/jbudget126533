package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Person;
import it.unicam.cs.mpgc.jbudget126533.model.PersonManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsTab implements Initializable {

    @FXML private TextField newPersonField;
    @FXML private ListView<String> peopleListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshPeople();
        ApplicationContext.registerController("settings", this);
    }

    @FXML public void addPerson(javafx.event.ActionEvent e) {
        String name = newPersonField.getText() == null ? "" : newPersonField.getText().trim();
        if (name.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Inserisci un nome!").showAndWait();
            return;
        }
        if (PersonManager.getPerson(name) != null) {
            new Alert(Alert.AlertType.ERROR, "Una persona con questo nome esiste già!").showAndWait();
            return;
        }
        PersonManager.addPerson(new Person(name));
        newPersonField.clear();
        refreshPeople();

        // notifica TransactionTab per aggiornare la ChoiceBox delle persone
        TransactionTab trx = ApplicationContext.getController("transactions", TransactionTab.class);
        if (trx != null) trx.refreshPersons();
    }

    @FXML public void backup(javafx.event.ActionEvent e) {
        new Alert(Alert.AlertType.INFORMATION, "Funzionalità in sviluppo!").showAndWait();
    }

    private void refreshPeople() {
        peopleListView.setItems(FXCollections.observableArrayList(PersonManager.getAllPersons().stream()
                .map(Person::getName).toList()));
    }
}
