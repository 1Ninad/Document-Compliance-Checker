package com.ieee.pdfchecker.cp3;
import java.io.File;
public class PdfChecker {
        private final RuleEngine ruleEngine;
        public PdfChecker() {
            this.ruleEngine = new RuleEngine();
        }

        public ComplianceReport analyzeFile(File file) {
            return ruleEngine.checkCompliance(file);
        }
}