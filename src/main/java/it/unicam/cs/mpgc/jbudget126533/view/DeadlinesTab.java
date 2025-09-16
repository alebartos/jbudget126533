package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.controller.DeadlineHandler;
import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.model.Deadline;
import it.unicam.cs.mpgc.jbudget126533.model.DeadlineType;
import it.unicam.cs.mpgc.jbudget126533.model.ScheduledTransaction;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class DeadlinesTab implements Initializable {

    @FXML private TableView<Deadline> deadlinesTable;
    @FXML private ChoiceBox<DeadlineType> deadlineFilterType;
    @FXML private Label totalDeadlinesLabel;
    @FXML private Label overdueDeadlinesLabel;
    @FXML private Label upcomingDeadlinesLabel; // presente in FXML

    // non presenti nel FXML corrente; passiamo null al handler (è safe per i setText)
    @FXML private Label dueTodayDeadlinesLabel;
    @FXML private Label futureDeadlinesLabel;

    // per refresh tabella transazioni programmate, come da costruttore originale
    @FXML private TableView<ScheduledTransaction> scheduledTable;

    private Ledger ledger;
    private DeadlineHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ledger = ApplicationContext.ledger();
        handler = new DeadlineHandler(ledger, deadlinesTable, deadlineFilterType,
                totalDeadlinesLabel, overdueDeadlinesLabel, dueTodayDeadlinesLabel, futureDeadlinesLabel, scheduledTable);
        handler.loadDeadlines();
        updateUpcomingCount();
        ApplicationContext.registerController("deadlines", this);
    }

    @FXML public void refreshDeadlines(javafx.event.ActionEvent e) {
        handler.refreshDeadlines(e);
        updateUpcomingCount();
    }

    @FXML public void processSelectedDeadline(javafx.event.ActionEvent e) {
        handler.processSelectedDeadline(e);
        updateUpcomingCount();
    }

    private void updateUpcomingCount() {
        if (upcomingDeadlinesLabel != null) {
            int upcoming = ledger.getUpcomingDeadlines().size();
            upcomingDeadlinesLabel.setText(String.valueOf(upcoming));
        }
    }
}
