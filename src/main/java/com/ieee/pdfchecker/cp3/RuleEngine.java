package com.ieee.pdfchecker.cp3;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

import java.awt.geom.Rectangle2D;



// NINAD
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.text.TextPosition;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class RuleEngine {

    public ComplianceReport checkCompliance(File file) {
        ComplianceReport report = new ComplianceReport(file.getName());

        try (PDDocument document = PDDocument.load(file)) {
            // CALL PRIVATE METHODS:

            //AMEY
            //checkReferenceFontSize(document, report);


            checkPageSize(document, report);
            checkColumnFormat(document, report); // NOT WORKING
            //checkColumnSpacing(document, report);

            // NINAD
            checkAbstractPresence(document, report);
            checkFont(document, report);
            //checkTitleSize(document, report);


            // PUSHKAR
            checkAbstractFormat(document, report);
            checkAuthorDetailsFormat(document, report);
            //checkAuthorAffiliationFormat(document, report);


            // ANIKET
            checkFontFormatting(document, report);





        } catch (IOException e) {
            report.addError("Error reading PDF: " + e.getMessage());
        }


        return report;
    }


    // ANISH
    private void checkPageSize(PDDocument document, ComplianceReport report) {
        PDPageTree pages = document.getDocumentCatalog().getPages();
        for (PDPage page : pages) {

        }

        for (PDPage page : pages) {
            Rectangle2D pageSize = new Rectangle2D.Float(
                    page.getMediaBox().getLowerLeftX(),
                    page.getMediaBox().getLowerLeftY(),
                    page.getMediaBox().getWidth(),
                    page.getMediaBox().getHeight()
            );

            // A4 size - 595x842 points, US Letter - 612x792 points
            boolean isA4 = (pageSize.getWidth() == 595 && pageSize.getHeight() == 842);
            boolean isLetter = (pageSize.getWidth() == 612 && pageSize.getHeight() == 792);

            if (!isA4 && !isLetter) {
                report.addError("Page size is incorrect. Must be A4 (595x842) or US Letter (612x792).");
            }
        }
    }

    private void checkColumnFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);

        String text = textStripper.getText(document);
        if (text.contains("Authors") || text.contains("Affiliations")) {
            int authorCount = text.split("\n").length;

            if (authorCount <= 3) {
                report.addInfo("Author affiliation section should have " + authorCount + " columns.");
            } else {
                report.addInfo("Author affiliation section should have a max of 3 columns, with rows adjusted accordingly.");
            }
        }
    }

    private void checkColumnSpacing(PDDocument document, ComplianceReport report) {
        float columnSpacing = 14.4f;
        float maxSpacing = 18.72f;

        double columnSpacingPoints = columnSpacing * 72;  // Convert from inches to points

        if (columnSpacingPoints >= 14.4 && columnSpacingPoints <= 18.72) {
            // ✅ Corrected range check
        } else {
            report.addError("Column spacing must be between 14.4 and 18.72 points.");
        }

    }




    // NINAD
    private void checkAbstractPresence(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(Math.min(2, document.getNumberOfPages()));

        String text = textStripper.getText(document);
        if (!text.toUpperCase().contains("ABSTRACT")) {
            report.addError("Abstract section is missing");
        }
    }


    private void checkFont(PDDocument document, ComplianceReport report) {
        Set<String> allowedFonts = new HashSet<>(Arrays.asList(
                "timesnewroman", "timesnewromanpsmt", "timesnewromanps-boldmt",
                "timesnewromanps-italicmt", "timesnewromanps-bolditalicmt", "times-roman"
        ));
        boolean foundValidFont = false;

        for (PDPage page : document.getPages()) {
            PDResources resources = page.getResources();
            if (resources == null) continue;

            Iterable<COSName> fontNamesIterable = resources.getFontNames();
            for (COSName fontName : fontNamesIterable) {
                try {
                    PDFont font = resources.getFont(fontName);
                    if (font != null) {
                        String fontLower = font.getName().toLowerCase().replaceAll("\\s+", "");
                        if (allowedFonts.contains(fontLower)) {
                            foundValidFont = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    report.addError("Error reading font metadata for: " + fontName.getName());
                }
            }

            if (foundValidFont) break;
        }

        if (!foundValidFont) {
            report.addError("Times New Roman font NOT detected in the document");
        }
    }


    private void checkTitleSize(PDDocument document, ComplianceReport report)  {
        try {
            PDPage firstPage = document.getPage(0);
            PDResources resources = firstPage.getResources();

            if (resources == null) {
                report.addError("No resources found on the first page");
                return;
            }

            Iterable<COSName> fontNamesIterable = resources.getFontNames();

            for (COSName fontName : fontNamesIterable) {
                PDFont font = resources.getFont(fontName);
                if (font != null && font.getFontDescriptor() != null) {
                    float fontSize = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000;

                    if (fontSize >= 100) {
                        return;
                    }
                }
            }

            report.addError("No text with font size 24PT found on first page");
        } catch (IOException e) {
            report.addError("Error reading font sizes: " + e.getMessage());
        }
    }




    // PUSHKAR
    private void checkAbstractFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(Math.min(2, document.getNumberOfPages()));

        String text = textStripper.getText(document);
        if (!text.toUpperCase().contains("ABSTRACT")) {
            report.addError("Abstract section is missing.");
        } else {
            int startIndex = text.indexOf("ABSTRACT") + 8;
            String remainingText = text.substring(startIndex).trim();
            int wordCount = remainingText.split("\\s+").length;

            if (wordCount < 100) {
                report.addError("Abstract must be at least 100 words.");
            }
        }
    }

    private void checkAuthorDetailsFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);

        String text = textStripper.getText(document);
        if (!text.contains("Author") && !text.contains("Authors")) {
            report.addError("Author details are missing.");
        }
    }

    private void checkAuthorAffiliationFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);

        String text = textStripper.getText(document);
        if (!text.contains("Affiliation")) {
            report.addError("Author affiliation details are missing.");
        }
    }



    // ANIKET
    private void checkFontFormatting(PDDocument document, ComplianceReport report) {
        AtomicBoolean foundAbstract = new AtomicBoolean(false);
        AtomicBoolean foundIntroduction = new AtomicBoolean(false);
        AtomicBoolean abstractIsValid = new AtomicBoolean(false);
        AtomicBoolean introductionIsValid = new AtomicBoolean(false);

        try {
            PDFTextStripper textStripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    super.writeString(text, textPositions);
                    processText(text, textPositions, foundAbstract, abstractIsValid, foundIntroduction, introductionIsValid);
                }
            };

            textStripper.getText(document);

            if (!foundAbstract.get()) {
                report.addError("⚠️ 'Abstract' section NOT FOUND!");
            } else if (!abstractIsValid.get()) {
                report.addError("⚠️ 'Abstract' does NOT meet IEEE formatting rules!");
            } else {
                report.addInfo("✅ 'Abstract' meets IEEE formatting rules.");
            }

            if (!foundIntroduction.get()) {
                report.addError("⚠️ 'Introduction' section NOT FOUND!");
            } else if (!introductionIsValid.get()) {
                report.addError("⚠️ 'Introduction' does NOT meet IEEE formatting rules!");
            } else {
                report.addInfo("✅ 'Introduction' meets IEEE formatting rules.");
            }

        } catch (IOException e) {
            report.addError("Error checking font formatting: " + e.getMessage());
        }
    }

    private void processText(String text, List<TextPosition> textPositions,
                             AtomicBoolean foundAbstract, AtomicBoolean abstractIsValid,
                             AtomicBoolean foundIntroduction, AtomicBoolean introductionIsValid) {
        String normalizedText = text.replaceAll("\\s+", " ").trim().toLowerCase();

        if (!foundAbstract.get() && normalizedText.contains("abstract")) {
            foundAbstract.set(true);
            abstractIsValid.set(checkAbstractFormatting(textPositions));
        }

        if (!foundIntroduction.get() && normalizedText.contains("introduction")) {
            foundIntroduction.set(true);
            introductionIsValid.set(checkIntroductionFormatting(textPositions));
        }
    }

    private boolean checkAbstractFormatting(List<TextPosition> textPositions) {
        boolean isBoldItalic = false;
        boolean isSize9pt = false;
        boolean isJustified = isTextJustified(textPositions);

        for (TextPosition position : textPositions) {
            float fontSize = position.getFontSizeInPt();

            if (fontSize == 9.0f) {
                isSize9pt = true;
            }

            if (position.getFont().getName().toLowerCase().contains("bold") &&
                    position.getFont().getName().toLowerCase().contains("italic")) {
                isBoldItalic = true;
            }
        }

        return isSize9pt && isBoldItalic && isJustified;
    }

    private boolean checkIntroductionFormatting(List<TextPosition> textPositions) {
        boolean isSize10pt = false;
        boolean isJustified = isTextJustified(textPositions);

        for (TextPosition position : textPositions) {
            float fontSize = position.getFontSizeInPt();

            if (fontSize == 10.0f) {
                isSize10pt = true;
                break;
            }
        }

        return isSize10pt && isJustified;
    }

    private boolean isTextJustified(List<TextPosition> textPositions) {
        if (textPositions.size() < 2) return false;

        float avgSpacing = 0;
        for (int i = 1; i < textPositions.size(); i++) {
            avgSpacing += Math.abs(textPositions.get(i).getX() - textPositions.get(i - 1).getEndX());
        }
        avgSpacing /= (textPositions.size() - 1);

        return avgSpacing < 2.0;
    }



}