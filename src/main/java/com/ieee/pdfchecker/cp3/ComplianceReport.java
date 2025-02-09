package com.ieee.pdfchecker.cp3;

import java.util.ArrayList;
import java.util.List;

public class ComplianceReport {
    private final String fileName;
    private final List<String> errors;
    private final List<String> infoMessages;

    public ComplianceReport(String fileName) {
        this.fileName = fileName;
        this.errors = new ArrayList<>();
        this.infoMessages = new ArrayList<>();
    }

    public void addError(String error) {
        errors.add(error);
    }

    public void addInfo(String info) {
        infoMessages.add(info);
    }

    public boolean isCompliant() {
        return errors.isEmpty();
    }

    public String getReportSummary() {
        StringBuilder report = new StringBuilder();
        report.append("PDF: ").append(fileName).append("\n");

        if (!errors.isEmpty()) {
            report.append("Errors:\n");
            for (String error : errors) {
                report.append("- ").append(error).append("\n");
            }
        } else {
            report.append("No errors found. The document is compliant.\n");
        }

        if (!infoMessages.isEmpty()) {
            report.append("\nAdditional Info:\n");
            for (String info : infoMessages) {
                report.append("- ").append(info).append("\n");
            }
        }

        return report.toString();
    }

}
