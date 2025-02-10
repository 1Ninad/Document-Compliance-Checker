package com.ieee.pdfchecker.cp3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;

import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlLocation = getClass().getResource("/com/ieee/pdfchecker/cp3/MainView.fxml");
            if (fxmlLocation == null) {
                throw new IOException("FXML file not found: /com/ieee/pdfchecker/cp3/MainView.fxml");
            }

            Parent root = FXMLLoader.load(fxmlLocation);

            // scrolling
            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            // window size
            primaryStage.setTitle("IEEE PDF Compliance Checker");
            primaryStage.setScene(new Scene(scrollPane, 600, 600)); // Set default width & height
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}


// IEEE Format PDF: /Users/ninadkale/Downloads/conference-template.pdf
// Springer: /Users/ninadkale/Downloads/Springer_Manuscript.pdf
// Non-IEEE: /Users/ninadkale/Downloads/Document12.pdf
