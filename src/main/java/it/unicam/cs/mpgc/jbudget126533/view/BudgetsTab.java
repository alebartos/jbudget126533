package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.controller.BudgetHandler;
import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.model.Budget;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class BudgetsTab implements Initializable {

    @FXML private ListView<ITag> budgetTagsListView;
    @FXML private TextField budgetAmountField;
    @FXML private DatePicker budgetStartDate;
    @FXML private DatePicker budgetEndDate;
    @FXML private TableView<Budget> budgetTable;


    private Ledger ledger;
    private BudgetHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ledger = ApplicationContext.ledger();
        handler = new BudgetHandler(ledger, budgetTagsListView, budgetAmountField, budgetStartDate, budgetEndDate, budgetTable);

        budgetTagsListView.setItems(ApplicationContext.selectedTags());
        budgetTagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        budgetStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        budgetEndDate.setValue(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));

        handler.updateBudgetTable();
        ApplicationContext.registerController("budgets", this);
    }

    @FXML public void setBudget(javafx.event.ActionEvent e)       { handler.setBudget(e); }
    @FXML public void updateBudgets(javafx.event.ActionEvent e)   { handler.updateBudgets(e); }
    @FXML public void removeBudget(javafx.event.ActionEvent e)    { handler.removeBudget(e); }
}
