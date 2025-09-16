package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.controller.ScheduledTransactionHandler;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import it.unicam.cs.mpgc.jbudget126533.model.MovementType;
import it.unicam.cs.mpgc.jbudget126533.model.RecurrenceType;
import it.unicam.cs.mpgc.jbudget126533.model.ScheduledTransaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ScheduledTab implements Initializable {

    @FXML private TextField scheduledDescField;
    @FXML private TextField scheduledAmountField;
    @FXML private ChoiceBox<MovementType> scheduledType;
    @FXML private ListView<ITag> scheduledTagsListView;
    @FXML private ChoiceBox<RecurrenceType> scheduledRecurrence;
    @FXML private DatePicker scheduledStartDate;
    @FXML private DatePicker scheduledEndDate;
    @FXML private TableView<ScheduledTransaction> scheduledTable;

    private Ledger ledger;
    private ScheduledTransactionHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ledger = ApplicationContext.ledger();
        handler = new ScheduledTransactionHandler(ledger, scheduledDescField, scheduledAmountField, scheduledType,
                scheduledTagsListView, scheduledRecurrence, scheduledStartDate, scheduledEndDate, scheduledTable);

        scheduledTagsListView.setItems(ApplicationContext.selectedTags());
        scheduledTagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        scheduledStartDate.setValue(LocalDate.now());
        handler.initializeChoiceBoxes();
        handler.updateScheduledTable();
        ApplicationContext.registerController("scheduled", this);
    }

    @FXML public void addScheduledTransaction(javafx.event.ActionEvent e)    { handler.addScheduledTransaction(e); }
    @FXML public void removeScheduledTransaction(javafx.event.ActionEvent e) { handler.removeScheduledTransaction(e); }
    @FXML public void checkScheduledTransactions(javafx.event.ActionEvent e) { handler.checkScheduledTransactions(e); }
}
