package com.ieee.pdfchecker.cp3;

import com.itextpdf.kernel.pdf.*;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;

import java.awt.geom.Rectangle2D;



// NINAD
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.text.TextPosition;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;


public class RuleEngine {

    public ComplianceReport checkCompliance(File file) {

        ComplianceReport report = new ComplianceReport(file.getName());

        try (PDDocument document = PDDocument.load(file)) {

            // METHODS:

            checkFont(document, report);
            checkColumnFormat(document, report); // longest

            checkPageSize(document, report);
            checkAbstractPresence(document, report);

            checkAbstractFormat(document, report);
            checkAuthorDetailsFormat(document, report);

            checkKeywordsFormat(document, report);
            IntroNumbering(document, report); // few lines

            // + JDBC

            // Use ComplianceReport data to extract values
            boolean abstractPresent = report.containsInfo("abstract");
            boolean fontCompliant = report.containsInfo("typeface"); // e.g., "Typeface (Times New Roman)"
            boolean columnFormatCompliant = report.containsInfo("column format compliant");
            boolean keywordsPresent = report.containsInfo("keywords section is present");
            boolean authorDetailsCompliant = report.containsInfo("author details appear properly formatted");
            boolean introNumberingValid = report.containsInfo("introduction section numbering is compliant");

// Extract title (you can update this logic to extract from PDF later)
            String title = "Untitled"; // placeholder for now

// Store in DB
            com.ieee.pdfchecker.db.DatabaseManager.insertComplianceLog(
                    file.getName(),
                    title,
                    abstractPresent,
                    fontCompliant,
                    columnFormatCompliant,
                    keywordsPresent,
                    authorDetailsCompliant,
                    introNumberingValid
            );





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

            boolean isA4 = (pageSize.getWidth() == 595 && pageSize.getHeight() == 842);
            boolean isLetter = (pageSize.getWidth() == 612 && pageSize.getHeight() == 792);

            if (!isA4 && !isLetter) {
                report.addError("Page size is incorrect. Must be A4 (595x842) or US Letter (612x792)");
            }
            else {
                report.addInfo("Page size is Compliant - A4 or US Letter");
            }
        }
    }

    private void checkColumnFormat(PDDocument document, ComplianceReport report) throws IOException {
        int numberOfPages = document.getNumberOfPages();
        boolean overallCompliant = true;
        float minCentroidSeparation = 50.0f;  // Minimum separation to treat as two distinct columns

        for (int page = 1; page <= numberOfPages; page++) {
            List<Float> firstWordPositions = new ArrayList<>();

            // Custom stripper to capture the x-coordinate of the first text element of each line
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                    if (textPositions != null && !textPositions.isEmpty()) {
                        firstWordPositions.add(textPositions.get(0).getXDirAdj());
                    }
                }
            };

            stripper.setStartPage(page);
            stripper.setEndPage(page);
            stripper.getText(document);

            if (firstWordPositions.isEmpty()) {
                report.addError("Page " + page + ": No text found.");
                overallCompliant = false;
                continue;
            }

            // Initialize centroids for k-means clustering (k=2) using the min and max x-positions
            float centroid1 = Collections.min(firstWordPositions);
            float centroid2 = Collections.max(firstWordPositions);

            if (centroid1 == centroid2) {
                report.addError("Page " + page + ": Column format not compliant, detected 1 column.");
                overallCompliant = false;
                continue;
            }

            // Perform simple 1D k-means clustering to group the positions into 2 clusters
            List<Float> cluster1 = new ArrayList<>();
            List<Float> cluster2 = new ArrayList<>();
            for (int iter = 0; iter < 100; iter++) {
                cluster1.clear();
                cluster2.clear();
                for (float pos : firstWordPositions) {
                    if (Math.abs(pos - centroid1) <= Math.abs(pos - centroid2)) {
                        cluster1.add(pos);
                    } else {
                        cluster2.add(pos);
                    }
                }
                float newCentroid1 = cluster1.isEmpty() ? centroid1 : average(cluster1);
                float newCentroid2 = cluster2.isEmpty() ? centroid2 : average(cluster2);
                if (Math.abs(newCentroid1 - centroid1) < 0.01f && Math.abs(newCentroid2 - centroid2) < 0.01f) {
                    centroid1 = newCentroid1;
                    centroid2 = newCentroid2;
                    break;
                }
                centroid1 = newCentroid1;
                centroid2 = newCentroid2;
            }

            // Check if the centroids are sufficiently separated to be considered two distinct columns
            if (Math.abs(centroid1 - centroid2) < minCentroidSeparation) {
                report.addError("Page " + page + ": Column format not compliant, detected 1 column.");
                overallCompliant = false;
            } else {
                report.addInfo("Page " + page + ": Column format compliant with 2 columns.");
            }
        }

        if (overallCompliant) {
            report.addInfo("Overall document column format compliant: two columns on every page.");
        } else {
            report.addError("Overall document column format not compliant.");
        }
    }

    private float average(List<Float> list) {
        float sum = 0;
        for (float value : list) {
            sum += value;
        }
        return sum / list.size();
    }




    // double
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
        boolean foundValidFont = false;
        Set<String> detectedFonts = new HashSet<>();
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            document.save(outStream);
            ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
            PdfReader reader = new PdfReader(inStream);
            PdfDocument pdfDoc = new PdfDocument(reader);
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage page = pdfDoc.getPage(i);
                PdfDictionary resources = page.getPdfObject().getAsDictionary(PdfName.Resources);
                if (resources == null) continue;
                PdfDictionary fonts = resources.getAsDictionary(PdfName.Font);
                if (fonts == null) continue;
                for (PdfName fontKey : fonts.keySet()) {
                    PdfDictionary fontDict = fonts.getAsDictionary(fontKey);
                    if (fontDict == null) continue;
                    PdfName baseFont = fontDict.getAsName(PdfName.BaseFont);
                    if (baseFont == null) continue;
                    String fontNameStr = baseFont.getValue();
                    String cleanFontName = fontNameStr.contains("+") ? fontNameStr.substring(fontNameStr.indexOf("+") + 1) : fontNameStr;
                    String normalizedFont = cleanFontName.toLowerCase().replaceAll("\\s+", "");
                    detectedFonts.add(cleanFontName);
                    if (normalizedFont.contains("times") && normalizedFont.contains("roman")) {
                        foundValidFont = true;
                        break;
                    }
                }
                if (foundValidFont) {
                    report.addInfo("Typeface (Times New Roman) is Compliant.");
                    break;
                }
            }
            pdfDoc.close();
        } catch (Exception e) {
            report.addError("Error checking font using iText: " + e.getMessage());
            return;
        }
        if (!foundValidFont) {
            String fontsList = String.join(", ", detectedFonts);
            report.addError("Times New Roman font not detected in the document. Detected fonts: " + fontsList);
        }
    }







    // PUSHKAR
    // WORKING
    private void checkAbstractFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(Math.min(2, document.getNumberOfPages()));

        String text = textStripper.getText(document);
        if (!text.toUpperCase().contains("ABSTRACT")) {
            report.addError("Abstract section is missing");
        }
        else report.addInfo("Abstract section is present");
    }

    // WORKING
    private void checkAuthorDetailsFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(1);

        String text = textStripper.getText(document);

        boolean hasSimpleName = Pattern.compile("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b").matcher(text).find();
        boolean hasAffiliation = text.toLowerCase().contains("department") || text.toLowerCase().contains("university");
        boolean hasEmail = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+").matcher(text).find();

        if (hasSimpleName && hasAffiliation) {
            report.addInfo("Author details are properly formatted — Compliant");
        } else {
            report.addError("Author details may be missing or incorrectly formatted — check name, affiliation, and structure.");
        }
    }





    // WORKING
    private void checkKeywordsFormat(PDDocument document, ComplianceReport report) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(1);
        textStripper.setEndPage(Math.min(2, document.getNumberOfPages()));

        String text = textStripper.getText(document).toLowerCase();
        if (text.contains("keywords") || text.contains("index terms")) {
            report.addInfo("Keywords section is present — Compliant.");
        } else {
            report.addError("Keywords section is missing.");
        }
    }





    // ANIKET
    private void IntroNumbering(PDDocument document, ComplianceReport report) {
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


            if (!foundIntroduction.get()) {
                report.addError("Introduction section not found");
            } else {
                if (introductionIsValid.get()) {
                    report.addInfo("Introduction section numbering is compliant");
                } else {
                    report.addError("Introduction section numbering is not compliant");
                }
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
            introductionIsValid.set(checkIntroductionFormatting(normalizedText));
        }
    }

    // DOUBLE
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

    private boolean checkIntroductionFormatting(String text) {
        Pattern pattern = Pattern.compile("^\\s*((\\d+)|([ivxlcdm]+))[\\.\\)]?\\s+introduction\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
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