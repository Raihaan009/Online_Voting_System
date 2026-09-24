package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.Voter;
import com.ovs.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link Voter} entities in the MySQL database.
 * Uses parameterized PreparedStatements to guarantee protection against SQL injection.
 */
public class VoterDAO {

    /**
     * Registers a new voter into the database.
     * Automatically hashes the voter's plaintext password with BCrypt via {@link PasswordUtil#hashPassword(String)}.
     *
     * @param voter the voter domain entity containing registration details
     * @return true if voter was successfully registered, false otherwise
     */
    public boolean registerVoter(Voter voter) {
        if (voter == null) {
            throw new IllegalArgumentException("Voter entity cannot be null.");
        }

        // Hash plaintext password using BCrypt before persisting
        String rawPassword = voter.getPasswordHash();
        String hashedPassword;
        if (rawPassword != null && (rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$") || rawPassword.startsWith("$2y$"))) {
            hashedPassword = rawPassword;
        } else {
            hashedPassword = PasswordUtil.hashPassword(rawPassword);
        }
        voter.setPasswordHash(hashedPassword);

        String sql = "INSERT INTO voters (name, email, password_hash, has_voted, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, voter.getName());
            pstmt.setString(2, voter.getEmail());
            pstmt.setString(3, voter.getPasswordHash());
            pstmt.setBoolean(4, voter.isHasVoted());
            pstmt.setString(5, voter.getStatus() != null ? voter.getStatus() : "APPROVED");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        voter.setVoterId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error registering voter: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticates a voter using email and plaintext password.
     * Fetches record by email and validates the candidate password against the stored BCrypt hash.
     *
     * @param email         voter's institutional email address
     * @param plainPassword candidate raw password entered during login
     * @return {@link Voter} entity if credentials are valid, or null if invalid / not found
     */
    public Voter login(String email, String plainPassword) {
        if (email == null || plainPassword == null) {
            return null;
        }

        Voter voter = getVoterByEmail(email);
        if (voter == null) {
            return null;
        }

        // Validate plaintext password candidate against stored BCrypt hash
        boolean passwordMatches = PasswordUtil.checkPassword(plainPassword, voter.getPasswordHash());
        if (passwordMatches) {
            return voter;
        }

        return null;
    }

    /**
     * Retrieves a voter by their unique ID.
     *
     * @param voterId primary key identifier of the voter
     * @return {@link Voter} domain model or null if not found
     */
    public Voter getVoterById(long voterId) {
        String sql = "SELECT voter_id, name, email, password_hash, has_voted, status, created_at FROM voters WHERE voter_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, voterId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoter(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching voter by ID (" + voterId + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a voter by their unique email address.
     *
     * @param email unique email address of the voter
     * @return {@link Voter} domain model or null if not found
     */
    public Voter getVoterByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT voter_id, name, email, password_hash, has_voted, status, created_at FROM voters WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVoter(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching voter by email (" + email + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates the general participation voting status of a voter.
     *
     * @param voterId  unique identifier of the voter
     * @param hasVoted true if the voter has cast a vote, false otherwise
     * @return true if updated successfully, false otherwise
     */
    public boolean updateHasVoted(long voterId, boolean hasVoted) {
        String sql = "UPDATE voters SET has_voted = ? WHERE voter_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, hasVoted);
            pstmt.setLong(2, voterId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating voter voting status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates a voter's account status (e.g. APPROVED, SUSPENDED, PENDING).
     *
     * @param voterId unique identifier of the voter
     * @param status  new account status
     * @return true if status was updated, false otherwise
     */
    public boolean updateStatus(long voterId, String status) {
        String sql = "UPDATE voters SET status = ? WHERE voter_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setLong(2, voterId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating voter status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a voter by ID (useful for administrative purging and automated testing cleanups).
     *
     * @param voterId unique identifier of the voter
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteVoter(long voterId) {
        String sql = "DELETE FROM voters WHERE voter_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, voterId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting voter by ID (" + voterId + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a voter by email (convenient for automated verification testing cleanup).
     *
     * @param email email of the voter to remove
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteVoterByEmail(String email) {
        if (email == null) return false;
        String sql = "DELETE FROM voters WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting voter by email (" + email + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all registered voters in the system.
     *
     * @return list of {@link Voter} entities
     */
    public List<Voter> getAllVoters() {
        List<Voter> voters = new ArrayList<>();
        String sql = "SELECT voter_id, name, email, password_hash, has_voted, status, created_at FROM voters ORDER BY voter_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                voters.add(mapResultSetToVoter(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all voters: " + e.getMessage());
        }
        return voters;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Voter} domain model.
     *
     * @param rs active ResultSet positioned at a valid row
     * @return populated {@link Voter} object
     * @throws SQLException if a column reading error occurs
     */
    private Voter mapResultSetToVoter(ResultSet rs) throws SQLException {
        return new Voter(
                rs.getLong("voter_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getBoolean("has_voted"),
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
