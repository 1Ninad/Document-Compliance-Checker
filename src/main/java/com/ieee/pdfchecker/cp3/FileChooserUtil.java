package com.ieee.pdfchecker.cp3;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class FileChooserUtil {

    public static File selectPdfFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        return fileChooser.showOpenDialog(new Stage());
    }
}