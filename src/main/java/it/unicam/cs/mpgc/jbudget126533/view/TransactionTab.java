package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.controller.TransactionHandler;
import it.unicam.cs.mpgc.jbudget126533.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TransactionTab implements Initializable {

    @FXML private ChoiceBox<MovementType> typeTransaction;
    @FXML private ChoiceBox<Person> personChoiceBox;
    @FXML private TextField moneyTransaction;
    @FXML private DatePicker dateTransaction;
    @FXML private TableView<ITransaction> transactionTable;
    @FXML private ListView<ITag> transactionTagsListView;

    private Ledger ledger;
    private TransactionHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.ledger = ApplicationContext.ledger();
        this.handler = new TransactionHandler(
                ledger, typeTransaction, personChoiceBox,
                moneyTransaction, dateTransaction, transactionTable,
                transactionTagsListView,  new Label()
        );

        transactionTagsListView.setItems(ApplicationContext.selectedTags());
        transactionTagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dateTransaction.setValue(LocalDate.now());
        handler.initializeChoiceBoxes();
        handler.loadTransactionTable();
        ApplicationContext.registerController("transactions", this);
    }

    @FXML public void addTransaction(javafx.event.ActionEvent e) {
        handler.addTransaction(e);

        DashboardTab dash = ApplicationContext.getController("dashboard", DashboardTab.class);
        if (dash != null) dash.updateBalance(null);
    }

    @FXML public void deleteSelectedTransaction(javafx.event.ActionEvent e) {
        handler.deleteSelectedTransaction();
        DashboardTab dash = ApplicationContext.getController("dashboard", DashboardTab.class);
        if (dash != null) dash.updateBalance(null);
    }

    @FXML public void showPersonManager(javafx.event.ActionEvent e) {
        ApplicationContext.nav().goToSettings();
    }

    public void refreshPersons() {
        if (personChoiceBox != null) {
            Person selected = personChoiceBox.getValue();
            personChoiceBox.setItems(FXCollections.observableArrayList(PersonManager.getAllPersons()));
            personChoiceBox.setValue(selected);
        }
    }
}
