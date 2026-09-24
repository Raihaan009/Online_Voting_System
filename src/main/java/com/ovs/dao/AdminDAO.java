package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.Admin;
import com.ovs.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link Admin} entities.
 * Implements administrative authentication, account provisioning, and role governance.
 */
public class AdminDAO {

    /**
     * Registers a new administrator into the system with BCrypt password hashing.
     *
     * @param admin administrator entity containing credentials and role
     * @return true if created successfully, false otherwise
     */
    public boolean registerAdmin(Admin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("Admin entity cannot be null.");
        }

        String rawPassword = admin.getPasswordHash();
        String hashedPassword;
        if (rawPassword != null && (rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$"))) {
            hashedPassword = rawPassword;
        } else {
            hashedPassword = PasswordUtil.hashPassword(rawPassword);
        }
        admin.setPasswordHash(hashedPassword);

        String sql = "INSERT INTO admin (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, admin.getName());
            pstmt.setString(2, admin.getEmail());
            pstmt.setString(3, admin.getPasswordHash());
            pstmt.setString(4, admin.getRole() != null ? admin.getRole() : "ELECTION_ADMIN");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        admin.setAdminId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates an administrator against stored BCrypt credentials.
     *
     * @param email         admin email address
     * @param plainPassword raw plaintext password
     * @return {@link Admin} entity if valid, null otherwise
     */
    public Admin login(String email, String plainPassword) {
        if (email == null || plainPassword == null) {
            return null;
        }

        Admin admin = getAdminByEmail(email);
        if (admin == null) {
            return null;
        }

        if (PasswordUtil.checkPassword(plainPassword, admin.getPasswordHash())) {
            return admin;
        }

        return null;
    }

    /**
     * Retrieves an administrator by ID.
     *
     * @param adminId unique identifier of the administrator
     * @return {@link Admin} entity or null if not found
     */
    public Admin getAdminById(long adminId) {
        String sql = "SELECT admin_id, name, email, password_hash, role, created_at FROM admin WHERE admin_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, adminId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdmin(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving admin by ID (" + adminId + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves an administrator by email address.
     *
     * @param email administrator's unique email address
     * @return {@link Admin} entity or null if not found
     */
    public Admin getAdminByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT admin_id, name, email, password_hash, role, created_at FROM admin WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdmin(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving admin by email (" + email + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all administrators registered in the system.
     *
     * @return list of {@link Admin} entities
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT admin_id, name, email, password_hash, role, created_at FROM admin ORDER BY admin_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                admins.add(mapResultSetToAdmin(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all admins: " + e.getMessage());
        }
        return admins;
    }

    /**
     * Maps the current row of a {@link ResultSet} into an {@link Admin} domain model.
     *
     * @param rs active ResultSet positioned at an admin row
     * @return populated {@link Admin} entity
     * @throws SQLException if column reading fails
     */
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        return new Admin(
                rs.getLong("admin_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getTimestamp("created_at")
        );
    }
}
