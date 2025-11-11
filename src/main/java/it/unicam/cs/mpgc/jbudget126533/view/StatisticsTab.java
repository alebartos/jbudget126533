package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import it.unicam.cs.mpgc.jbudget126533.controller.StatisticsHandler;
import it.unicam.cs.mpgc.jbudget126533.model.MovementType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticsTab implements Initializable {

    @FXML private Label balanceForRange;
    @FXML private DatePicker dateStartForRange;
    @FXML private DatePicker dateEndForRange;
    @FXML private ChoiceBox<MovementType> choiceForRange;

    @FXML private Label balanceTrend;
    @FXML private DatePicker dateStartForTrend;
    @FXML private DatePicker dateEndForTrend;

    @FXML private TableView<Map.Entry<String, Double>> tagTable;
    @FXML private ChoiceBox<MovementType> choiceTypeForEachTag;

    private Ledger ledger;
    private StatisticsHandler handler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ledger = ApplicationContext.ledger();
        handler = new StatisticsHandler(ledger, balanceForRange, dateStartForRange, dateEndForRange,
                choiceForRange, balanceTrend, dateStartForTrend, dateEndForTrend, tagTable, choiceTypeForEachTag);


        dateStartForRange.setValue(LocalDate.now().minusMonths(1));
        dateEndForRange.setValue(LocalDate.now());
        dateStartForTrend.setValue(LocalDate.now().minusMonths(3));
        dateEndForTrend.setValue(LocalDate.now());

        handler.initializeChoiceBoxes();
        ApplicationContext.registerController("statistics", this);
    }

    @FXML public void updateBalanceForRange(javafx.event.ActionEvent e) { handler.updateBalanceForRange(e); }
    @FXML public void updateBalanceTrend(javafx.event.ActionEvent e)    { handler.updateBalanceTrend(e); }
    @FXML public void showTypeTagTable(javafx.event.ActionEvent e)      { handler.showTypeTagTable(e); }
}
