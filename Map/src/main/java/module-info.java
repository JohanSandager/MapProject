module view {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens view to javafx.fxml;
    exports view;
}