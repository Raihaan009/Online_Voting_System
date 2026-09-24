package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for the Administrative Governance & Security Audit Trail.
 * Records immutable audit logs for all administrative actions and security-critical events.
 */
public class AuditDAO {

    public AuditDAO() {
        initTable();
    }

    /**
     * Initializes the audit_logs table if it does not yet exist in the MySQL database.
     */
    private void initTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, " +
                "admin_email VARCHAR(150) NOT NULL, " +
                "action_type VARCHAR(50) NOT NULL, " +
                "details TEXT, " +
                "ip_address VARCHAR(45) DEFAULT NULL, " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (log_id), " +
                "KEY idx_audit_admin (admin_email), " +
                "KEY idx_audit_created (created_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        } catch (SQLException e) {
            System.err.println("Warning: Unable to verify/create audit_logs table: " + e.getMessage());
        }
    }

    /**
     * Inserts an immutable audit log entry into the database.
     *
     * @param adminEmail administrator institutional email
     * @param actionType categorized action (e.g., ADMIN_LOGIN, CREATE_ELECTION, NOMINATE_CANDIDATE)
     * @param details    metadata describing the operation
     * @param ipAddress  client IP address
     * @return true if successfully inserted, false otherwise
     */
    public boolean logAction(String adminEmail, String actionType, String details, String ipAddress) {
        String sql = "INSERT INTO audit_logs (admin_email, action_type, details, ip_address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminEmail != null ? adminEmail.trim().toLowerCase() : "SYSTEM");
            pstmt.setString(2, actionType != null ? actionType.trim().toUpperCase() : "UNKNOWN_ACTION");
            pstmt.setString(3, details != null ? details : "");
            pstmt.setString(4, ipAddress != null ? ipAddress : "127.0.0.1");

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error recording audit log entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the most recent audit logs ordered by creation timestamp descending.
     *
     * @param limit maximum number of logs to return
     * @return list of {@link AuditLog} instances
     */
    public List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        int safeLimit = limit > 0 ? limit : 100;
        String sql = "SELECT log_id, admin_email, action_type, details, ip_address, created_at " +
                     "FROM audit_logs ORDER BY created_at DESC, log_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, safeLimit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog(
                            rs.getLong("log_id"),
                            rs.getString("admin_email"),
                            rs.getString("action_type"),
                            rs.getString("details"),
                            rs.getString("ip_address"),
                            rs.getTimestamp("created_at")
                    );
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent audit logs: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Retrieves the total count of recorded audit entries in the system.
     *
     * @return total log count
     */
    public long getTotalLogCount() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total audit log count: " + e.getMessage());
        }
        return 0;
    }
}
