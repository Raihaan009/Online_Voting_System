package com.ovs.util;

import com.ovs.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Administrative Database Seeder Utility.
 * Seeds a verified super-administrator account into the MySQL database with
 * 12-round BCrypt cryptographic password hashing.
 *
 * Guaranteed Super Admin Credentials:
 *   Email    : admin@college.edu
 *   Password : Admin@123
 *   Role     : SUPER_ADMIN
 */
public class AdminSeeder {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static final String DEFAULT_ADMIN_EMAIL = "admin@college.edu";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    public static final String DEFAULT_ADMIN_NAME = "Chief Election Commissioner";
    public static final String DEFAULT_ADMIN_ROLE = "SUPER_ADMIN";

    public static void main(String[] args) {
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);
        System.out.println(BOLD + CYAN + "          ONLINE VOTING SYSTEM - ADMINISTRATOR DATABASE SEEDER                 " + RESET);
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);

        boolean success = seedAdmin(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD, DEFAULT_ADMIN_NAME, DEFAULT_ADMIN_ROLE);

        if (success) {
            System.out.println("\n" + BOLD + GREEN + ">>> ADMINISTRATOR ACCOUNT PROVISIONED SUCCESSFULLY! <<<" + RESET);
            System.out.println(BOLD + "Login URL       : " + RESET + "http://localhost:8080/online-voting-system/admin/login.jsp");
            System.out.println(BOLD + "Admin Email     : " + RESET + GREEN + DEFAULT_ADMIN_EMAIL + RESET);
            System.out.println(BOLD + "Admin Password  : " + RESET + GREEN + DEFAULT_ADMIN_PASSWORD + RESET);
            System.out.println(BOLD + "Role Assigned   : " + RESET + DEFAULT_ADMIN_ROLE);
            System.out.println("================================================================================");
        } else {
            System.err.println("\n>>> FAILED TO PROVISION ADMINISTRATOR ACCOUNT. PLEASE CHECK LOGS. <<<");
            System.exit(1);
        }
    }

    /**
     * Seeds or updates an administrator account in the MySQL database.
     *
     * @param email         admin institutional email address
     * @param plainPassword raw candidate password to be hashed
     * @param name          full name of the administrator
     * @param role          role title (SUPER_ADMIN or ELECTION_ADMIN)
     * @return true if seeded successfully, false otherwise
     */
    public static boolean seedAdmin(String email, String plainPassword, String name, String role) {
        if (email == null || plainPassword == null) {
            System.err.println("Admin email and password cannot be null.");
            return false;
        }

        // Generate strong 12-round BCrypt salt and hash
        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        System.out.println("Generated BCrypt 12-Round Hash: " + passwordHash);

        String sql = "INSERT INTO admin (name, email, password_hash, role) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "  name = VALUES(name), " +
                     "  password_hash = VALUES(password_hash), " +
                     "  role = VALUES(role)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email.trim().toLowerCase());
            pstmt.setString(3, passwordHash);
            pstmt.setString(4, role != null ? role : "SUPER_ADMIN");

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Database upsert executed. Rows affected: " + affectedRows);

            // Double check by reading back from DB and verifying BCrypt hash
            String verifySql = "SELECT admin_id, name, email, password_hash, role FROM admin WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(verifySql)) {
                checkStmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        long adminId = rs.getLong("admin_id");
                        String storedHash = rs.getString("password_hash");
                        boolean match = PasswordUtil.checkPassword(plainPassword, storedHash);

                        if (match) {
                            System.out.println(GREEN + "Cryptographic verification confirmed! Admin ID: " + adminId + RESET);
                            return true;
                        } else {
                            System.err.println("Fatal: Stored password hash did not verify against candidate password!");
                            return false;
                        }
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error seeding administrator account: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
