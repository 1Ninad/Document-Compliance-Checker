package com.ieee.pdfchecker.cp3;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainController {
    @FXML
    private TextArea resultArea;

    private final PdfChecker pdfChecker = new PdfChecker();

    //NEW
    @FXML
    private Label resultLabel;
    @FXML
    private void handleFileSelection() { // This method was missing!
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            ComplianceReport report = pdfChecker.analyzeFile(file);
            resultLabel.setText(report.getReportSummary()); // Updated to use Label
        }
    } //CLOSE

    @FXML
    private void handleCheckCompliance() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            ComplianceReport report = pdfChecker.analyzeFile(file);
            resultArea.setText(report.getReportSummary());
        }
    }
}