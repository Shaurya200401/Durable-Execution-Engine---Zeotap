package com.zeotap.durable.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceLayer {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceLayer.class);
    private static final String DB_URL = "jdbc:sqlite:durable.db";
    private static final String TABLE_NAME = "steps";

    public PersistenceLayer() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "workflow_id TEXT NOT NULL, " +
                        "step_key TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "output TEXT, " +
                        "PRIMARY KEY (workflow_id, step_key)" +
                        ");";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException(e);
        }
    }

    public synchronized void saveStep(String workflowId, String stepKey, String status, String output) {
        String sql = "INSERT OR REPLACE INTO " + TABLE_NAME + "(workflow_id, step_key, status, output) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, workflowId);
            pstmt.setString(2, stepKey);
            pstmt.setString(3, status);
            pstmt.setString(4, output);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to save step result", e);
            throw new RuntimeException(e);
        }
    }

    public synchronized String getStepOutput(String workflowId, String stepKey) {
        String sql = "SELECT output FROM " + TABLE_NAME + " WHERE workflow_id = ? AND step_key = ? AND status = 'COMPLETED'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, workflowId);
            pstmt.setString(2, stepKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("output");
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get step result", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public synchronized boolean isStepCompleted(String workflowId, String stepKey) {
        String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE workflow_id = ? AND step_key = ? AND status = 'COMPLETED'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, workflowId);
            pstmt.setString(2, stepKey);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Failed to check step status", e);
            throw new RuntimeException(e);
        }
    }
}
