module com.project.cybershield {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.project.cybershield to javafx.fxml;
    exports com.project.cybershield;
}