package it.unicam.cs.mpgc.jbudget126533.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainTabController implements Initializable, NavigationService {

    @FXML private TabPane mainTabPane;

    // Indici coerenti con l'ordine dei <Tab> in MainTab.fxml
    private static final int DASHBOARD = 0;
    private static final int TRANSACTIONS = 1;
    private static final int TAGS = 2;
    private static final int STATISTICS = 3;
    private static final int BUDGETS = 4;
    private static final int SCHEDULED = 5;
    private static final int AMORTIZATION = 6;
    private static final int DEADLINES = 7;
    private static final int SETTINGS = 8;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationContext.setNavigationService(this);
    }

    @Override public void goToDashboard()    { mainTabPane.getSelectionModel().select(DASHBOARD); }
    @Override public void goToTransactions() { mainTabPane.getSelectionModel().select(TRANSACTIONS); }
    @Override public void goToTags()         { mainTabPane.getSelectionModel().select(TAGS); }
    @Override public void goToStatistics()   { mainTabPane.getSelectionModel().select(STATISTICS); }
    @Override public void goToBudgets()      { mainTabPane.getSelectionModel().select(BUDGETS); }
    @Override public void goToScheduled()    { mainTabPane.getSelectionModel().select(SCHEDULED); }
    @Override public void goToAmortization() { mainTabPane.getSelectionModel().select(AMORTIZATION); }
    @Override public void goToDeadlines()    { mainTabPane.getSelectionModel().select(DEADLINES); }
    @Override public void goToSettings()     { mainTabPane.getSelectionModel().select(SETTINGS); }
}
