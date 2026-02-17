package com.zeotap.durable.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDb {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:durable.db";
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT step_key FROM steps WHERE workflow_id = 'auto-test-direct' ORDER BY step_key")) {

            while (rs.next()) {
                String key = rs.getString("step_key");
                System.out.println("STEP:" + key.substring(key.lastIndexOf(":") + 1));
            }
        }
    }
}
