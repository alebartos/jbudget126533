package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.controller.AmortizationHandler;
import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.model.AmortizationPlan;
import it.unicam.cs.mpgc.jbudget126533.model.ITag;
import it.unicam.cs.mpgc.jbudget126533.model.Installment;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class AmortizationTab implements Initializable {

    @FXML private TextField amortDescription;
    @FXML private TextField amortPrincipal;
    @FXML private TextField amortInterestRate;
    @FXML private TextField amortInstallments;
    @FXML private DatePicker amortStartDate;
    @FXML private ListView<ITag> amortTags;
    @FXML private TableView<Installment> amortTable;
    @FXML private TableView<AmortizationPlan> amortizationPlansTable;


    private Ledger ledger;
    private AmortizationHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ledger = ApplicationContext.ledger();
        handler = new AmortizationHandler(ledger, amortDescription, amortPrincipal, amortInterestRate, amortInstallments,
                amortStartDate, amortTags, amortTable, amortizationPlansTable);

        amortTags.setItems(ApplicationContext.selectedTags());
        amortTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        handler.loadAmortizationPlans();
        ApplicationContext.registerController("amortization", this);
    }

    @FXML public void createAmortizationPlan(javafx.event.ActionEvent e) { handler.createAmortizationPlan(e); }
    @FXML public void refreshAmortizationTable(javafx.event.ActionEvent e) { handler.refreshAmortizationTable(e); }
    @FXML public void deleteSelectedAmortizationPlan(javafx.event.ActionEvent e) { handler.deleteSelectedAmortizationPlan(e); }
}
