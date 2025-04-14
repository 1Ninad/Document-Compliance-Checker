package com.ieee.pdfchecker.cp3;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainController {
    @FXML
    private TextArea resultArea;
    @FXML
    private Button viewLogsButton;

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

    @FXML
    private void onViewLogsClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ieee/pdfchecker/view_logs.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Previous Uploads");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}