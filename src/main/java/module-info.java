module net.reini.swissfxknife {
    requires java.logging;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bouncycastle.provider;

    opens net.reini.swissfxknife.controller to javafx.fxml;
    exports net.reini.swissfxknife;
}

