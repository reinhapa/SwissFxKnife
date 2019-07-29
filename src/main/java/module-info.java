module net.reini.swissfxknife {
    requires java.logging;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;

    opens net.reini.swissfxknife.controller to javafx.fxml, javafx.base;
    exports net.reini.swissfxknife;
}

