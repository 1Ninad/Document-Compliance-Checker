package com.ieee.pdfchecker.db;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.ieee.pdfchecker.cp3.ComplianceLog;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Oops";
    private static final String USER = "root";
    private static final String PASSWORD = "Ninadk12"; // change to your actual password

    public static void insertComplianceLog(
            String fileName,
            String title,
            boolean abstractPresent,
            boolean fontCompliant,
            boolean columnFormatCompliant,
            boolean keywordsPresent,
            boolean authorDetailsCompliant,
            boolean introNumberingValid
    ) {
        String query = "INSERT INTO pdf_compliance_logs " +
                "(file_name, title, abstract_present, font_compliant, column_format_compliant, keywords_present, author_details_compliant, intro_numbering_valid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fileName);
            stmt.setString(2, title);
            stmt.setBoolean(3, abstractPresent);
            stmt.setBoolean(4, fontCompliant);
            stmt.setBoolean(5, columnFormatCompliant);
            stmt.setBoolean(6, keywordsPresent);
            stmt.setBoolean(7, authorDetailsCompliant);
            stmt.setBoolean(8, introNumberingValid);

            stmt.executeUpdate();
            System.out.println("Compliance result inserted into database.");

        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
    }

    public static List<ComplianceLog> fetchAllLogs() {
        List<ComplianceLog> logs = new ArrayList<>();
        String query = "SELECT * FROM pdf_compliance_logs ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ComplianceLog log = new ComplianceLog(
                        rs.getString("file_name"),
                        rs.getBoolean("abstract_present"),
                        rs.getBoolean("font_compliant"),
                        rs.getBoolean("column_format_compliant"),
                        rs.getBoolean("keywords_present"),
                        rs.getBoolean("author_details_compliant"),
                        rs.getBoolean("intro_numbering_valid"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching logs: " + e.getMessage());
        }

        return logs;
    }
}