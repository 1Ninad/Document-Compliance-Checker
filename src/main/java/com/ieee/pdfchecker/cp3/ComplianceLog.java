package com.ieee.pdfchecker.cp3;

import java.time.LocalDateTime;

public class ComplianceLog {
    private String fileName;
    private boolean abstractPresent;
    private boolean fontCompliant;
    private boolean columnFormatCompliant;
    private boolean keywordsPresent;
    private boolean authorDetailsCompliant;
    private boolean introNumberingValid;
    private LocalDateTime createdAt;

    public ComplianceLog(String fileName, boolean abstractPresent, boolean fontCompliant,
                         boolean columnFormatCompliant, boolean keywordsPresent,
                         boolean authorDetailsCompliant, boolean introNumberingValid,
                         LocalDateTime createdAt) {
        this.fileName = fileName;
        this.abstractPresent = abstractPresent;
        this.fontCompliant = fontCompliant;
        this.columnFormatCompliant = columnFormatCompliant;
        this.keywordsPresent = keywordsPresent;
        this.authorDetailsCompliant = authorDetailsCompliant;
        this.introNumberingValid = introNumberingValid;
        this.createdAt = createdAt;
    }

    // Add getters (JavaFX TableView uses them)
    public String getFileName() { return fileName; }
    public boolean isAbstractPresent() { return abstractPresent; }
    public boolean isFontCompliant() { return fontCompliant; }
    public boolean isColumnFormatCompliant() { return columnFormatCompliant; }
    public boolean isKeywordsPresent() { return keywordsPresent; }
    public boolean isAuthorDetailsCompliant() { return authorDetailsCompliant; }
    public boolean isIntroNumberingValid() { return introNumberingValid; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}