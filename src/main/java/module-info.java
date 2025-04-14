module com.ieee.pdfchecker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires kernel;
    requires java.sql;

    opens com.ieee.pdfchecker.cp3 to javafx.fxml;
    exports com.ieee.pdfchecker.cp3;
}