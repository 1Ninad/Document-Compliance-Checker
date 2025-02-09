module com.ieee.pdfchecker.cp3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.ieee.pdfchecker.cp3 to javafx.fxml;
    exports com.ieee.pdfchecker.cp3;
}