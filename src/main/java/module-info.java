module mx.nanosip.nanosip {
    requires javafx.controls;
    requires javafx.fxml;


    opens mx.nanosip.nanosip to javafx.fxml;
    exports mx.nanosip.nanosip;
}