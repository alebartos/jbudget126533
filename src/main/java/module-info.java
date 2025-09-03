module it.unicam.cs.mpgc.jbudget {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.logging;

    opens it.unicam.cs.mpgc.jbudget126533 to javafx.graphics;
    opens it.unicam.cs.mpgc.jbudget126533.controller to javafx.fxml;
    opens it.unicam.cs.mpgc.jbudget126533.model to com.google.gson;
    opens it.unicam.cs.mpgc.jbudget126533.view to javafx.fxml;

    exports it.unicam.cs.mpgc.jbudget126533;
    exports it.unicam.cs.mpgc.jbudget126533.controller;
    exports it.unicam.cs.mpgc.jbudget126533.model;
    exports it.unicam.cs.mpgc.jbudget126533.view;
    exports it.unicam.cs.mpgc.jbudget126533.util;
    opens it.unicam.cs.mpgc.jbudget126533.util to com.google.gson;
}