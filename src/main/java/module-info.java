module mx.nanosip.nanosip {
    requires javafx.controls;
    requires javafx.fxml;

    opens mx.nanosip.nanosip to javafx.fxml;
    exports mx.nanosip.nanosip;
    exports mx.nanosip.nanosip.Controllers;
    opens mx.nanosip.nanosip.Controllers to javafx.fxml;
    exports mx.nanosip.nanosip.Controllers.Modals;
    opens mx.nanosip.nanosip.Controllers.Modals to javafx.fxml;
}