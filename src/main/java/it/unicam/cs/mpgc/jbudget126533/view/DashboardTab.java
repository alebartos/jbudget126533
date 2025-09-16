package it.unicam.cs.mpgc.jbudget126533.view;

import it.unicam.cs.mpgc.jbudget126533.model.Ledger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class DashboardTab implements Initializable {

    @FXML private Label balance;
    @FXML private ImageView image;

    private Ledger ledger;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.ledger = ApplicationContext.ledger();
        updateBalance(null);
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("jbudget.jpg")));
            image.setImage(img);
        } catch (Exception ignored) {}
        ApplicationContext.registerController("dashboard", this);
    }

    @FXML
    public void updateBalance(javafx.event.ActionEvent e) {
        balance.setText(String.format("%.2f €", ledger.getBalance()));
    }

    @FXML public void showAllTransactions(javafx.event.ActionEvent e) { ApplicationContext.nav().goToTransactions(); }
    @FXML public void showBudget(javafx.event.ActionEvent e)          { ApplicationContext.nav().goToBudgets(); }
    @FXML public void showTransazioniProgrammate(javafx.event.ActionEvent e) { ApplicationContext.nav().goToScheduled(); }
    @FXML public void showStatistics(javafx.event.ActionEvent e)      { ApplicationContext.nav().goToStatistics(); }
    @FXML public void showAmortization(javafx.event.ActionEvent e)    { ApplicationContext.nav().goToAmortization(); }
    @FXML public void showTagManager(javafx.event.ActionEvent e)      { ApplicationContext.nav().goToTags(); }
    @FXML public void showDeadlines(javafx.event.ActionEvent e)       { ApplicationContext.nav().goToDeadlines(); }
    @FXML public void showSettings(javafx.event.ActionEvent e)        { ApplicationContext.nav().goToSettings(); }
}
