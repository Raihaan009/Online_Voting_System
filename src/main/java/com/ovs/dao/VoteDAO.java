package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.CandidateResult;
import com.ovs.models.Vote;
import com.ovs.models.VoterElectionStatus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) for managing secret ballots, voting receipts, and voter participation status.
 * Executes atomic, ACID-compliant transactions ensuring secret ballot anonymity,
 * one-person-one-vote rules, and SHA-256 cryptographic receipt verification.
 */
public class VoteDAO {

    /**
     * Casts an immutable secret ballot within an ACID transaction.
     * Enforces the one-person-one-vote constraint by:
     * 1. Verifying voter has not already cast a ballot in the specified election.
     * 2. Generating a cryptographic SHA-256 digital receipt token from voterId, electionId, and timestamp.
     * 3. Statement 1: Inserting the anonymous secret ballot into {@code votes} (omitting voter identity).
     * 4. Statement 2: Recording voter participation in {@code voter_election_status}.
     * 5. Statement 3: Updating global {@code has_voted} flag on the voter record.
     * 6. Committing transaction and returning the receipt token.
     *
     * @param voterId     unique identifier of the voter casting the ballot
     * @param electionId  target election ID
     * @param candidateId selected candidate ID
     * @return 64-character SHA-256 digital receipt verification token
     * @throws SQLException if a duplicate vote is attempted or a database error occurs
     */
    public String castVote(long voterId, long electionId, long candidateId) throws SQLException {
        String checkStatusSql = "SELECT has_voted FROM voter_election_status WHERE voter_id = ? AND election_id = ?";
        String insertVoteSql = "INSERT INTO votes (election_id, candidate_id, receipt_token) VALUES (?, ?, ?)";
        String recordStatusSql = "INSERT INTO voter_election_status (voter_id, election_id, has_voted, voted_at, receipt_token) " +
                                 "VALUES (?, ?, 1, NOW(), ?) " +
                                 "ON DUPLICATE KEY UPDATE has_voted = 1, voted_at = NOW(), receipt_token = VALUES(receipt_token)";
        String updateVoterFlagSql = "UPDATE voters SET has_voted = 1 WHERE voter_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction

            // Step 1: Double-voting prevention check
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStatusSql)) {
                checkStmt.setLong(1, voterId);
                checkStmt.setLong(2, electionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getBoolean("has_voted")) {
                        conn.rollback();
                        throw new SQLException("Duplicate vote rejected: Voter ID " + voterId + 
                                               " has already cast a ballot in election ID " + electionId);
                    }
                }
            }

            // Step 2: Generate cryptographic SHA-256 digital receipt token
            String receiptToken = generateReceiptToken(voterId, electionId);

            // Statement 1: Insert into votes (election_id, candidate_id, receipt_token)
            // CRITICAL: voter_id is strictly omitted here to protect secret ballot privacy
            try (PreparedStatement voteStmt = conn.prepareStatement(insertVoteSql)) {
                voteStmt.setLong(1, electionId);
                voteStmt.setLong(2, candidateId);
                voteStmt.setString(3, receiptToken);
                int affectedRows = voteStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Failed to record vote ballot into votes table.");
                }
            }

            // Statement 2: Insert/Update voter_election_status for participation tracking
            try (PreparedStatement statusStmt = conn.prepareStatement(recordStatusSql)) {
                statusStmt.setLong(1, voterId);
                statusStmt.setLong(2, electionId);
                statusStmt.setString(3, receiptToken);
                statusStmt.executeUpdate();
            }

            // Statement 3: Update voters SET has_voted = TRUE WHERE voter_id = ?
            try (PreparedStatement voterStmt = conn.prepareStatement(updateVoterFlagSql)) {
                voterStmt.setLong(1, voterId);
                voterStmt.executeUpdate();
            }

            // Commit atomic transaction
            conn.commit();
            return receiptToken;

        } catch (SQLException e) {
            System.err.println("Transaction failed in VoteDAO.castVote: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Critical error rolling back vote transaction: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException resetEx) {
                    System.err.println("Error resetting connection autocommit: " + resetEx.getMessage());
                }
            }
        }
    }

    /**
     * Backward-compatible overload accepting a pre-generated receipt token.
     *
     * @param voterId      unique identifier of the voter casting the ballot
     * @param electionId   target election ID
     * @param candidateId  selected candidate ID
     * @param receiptToken 64-character SHA-256 cryptographic receipt reference
     * @return true if vote was successfully committed, false otherwise
     */
    public boolean castVote(long voterId, long electionId, long candidateId, String receiptToken) {
        if (receiptToken == null || receiptToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Cryptographic receipt token cannot be null or empty.");
        }

        String checkStatusSql = "SELECT has_voted FROM voter_election_status WHERE voter_id = ? AND election_id = ?";
        String insertVoteSql = "INSERT INTO votes (election_id, candidate_id, receipt_token) VALUES (?, ?, ?)";
        String recordStatusSql = "INSERT INTO voter_election_status (voter_id, election_id, has_voted, voted_at, receipt_token) " +
                                 "VALUES (?, ?, 1, NOW(), ?) " +
                                 "ON DUPLICATE KEY UPDATE has_voted = 1, voted_at = NOW(), receipt_token = VALUES(receipt_token)";
        String updateVoterFlagSql = "UPDATE voters SET has_voted = 1 WHERE voter_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction

            // 1. Verify eligibility
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStatusSql)) {
                checkStmt.setLong(1, voterId);
                checkStmt.setLong(2, electionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getBoolean("has_voted")) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Insert ballot into votes table (decoupled from voter identity)
            try (PreparedStatement voteStmt = conn.prepareStatement(insertVoteSql)) {
                voteStmt.setLong(1, electionId);
                voteStmt.setLong(2, candidateId);
                voteStmt.setString(3, receiptToken);
                int voteInserted = voteStmt.executeUpdate();
                if (voteInserted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Record participation tracking status
            try (PreparedStatement statusStmt = conn.prepareStatement(recordStatusSql)) {
                statusStmt.setLong(1, voterId);
                statusStmt.setLong(2, electionId);
                statusStmt.setString(3, receiptToken);
                statusStmt.executeUpdate();
            }

            // 4. Update global voter flag
            try (PreparedStatement voterStmt = conn.prepareStatement(updateVoterFlagSql)) {
                voterStmt.setLong(1, voterId);
                voterStmt.executeUpdate();
            }

            // Commit atomic transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed while casting vote: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back vote transaction: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException resetEx) {
                    System.err.println("Error resetting connection autocommit: " + resetEx.getMessage());
                }
            }
        }
    }

    /**
     * Tabulates aggregated vote results for all candidates participating in a specified election.
     * Groups counts by {@code candidate_id} and orders from highest vote count descending.
     *
     * @param electionId unique identifier of the target election
     * @return list of {@link CandidateResult} DTO projections
     */
    public List<CandidateResult> getElectionResults(long electionId) {
        List<CandidateResult> results = new ArrayList<>();
        String sql = "SELECT c.candidate_id, c.name AS candidate_name, c.party_symbol, c.election_id, " +
                     "       COUNT(v.vote_id) AS total_votes " +
                     "FROM candidates c " +
                     "LEFT JOIN votes v ON c.candidate_id = v.candidate_id AND v.election_id = ? " +
                     "WHERE c.election_id = ? " +
                     "GROUP BY c.candidate_id, c.name, c.party_symbol, c.election_id " +
                     "ORDER BY total_votes DESC, c.name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, electionId);
            pstmt.setLong(2, electionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new CandidateResult(
                            rs.getLong("candidate_id"),
                            rs.getString("candidate_name"),
                            rs.getString("party_symbol"),
                            rs.getLong("election_id"),
                            rs.getLong("total_votes")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving election results for election ID (" + electionId + "): " + e.getMessage());
        }
        return results;
    }

    /**
     * Checks if a voter has already cast a vote in a specific election.
     *
     * @param voterId    unique identifier of the voter
     * @param electionId unique identifier of the election
     * @return true if the voter has voted, false otherwise
     */
    public boolean hasVoterVotedInElection(long voterId, long electionId) {
        String sql = "SELECT has_voted FROM voter_election_status WHERE voter_id = ? AND election_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, voterId);
            pstmt.setLong(2, electionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_voted");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking voter election status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves secret ballot details by its cryptographic receipt token.
     *
     * @param receiptToken 64-character SHA-256 ballot receipt token
     * @return {@link Vote} entity or null if not found
     */
    public Vote getVoteByReceiptToken(String receiptToken) {
        if (receiptToken == null || receiptToken.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT vote_id, election_id, candidate_id, vote_timestamp, receipt_token FROM votes WHERE receipt_token = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, receiptToken.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Vote(
                            rs.getLong("vote_id"),
                            rs.getLong("election_id"),
                            rs.getLong("candidate_id"),
                            rs.getTimestamp("vote_timestamp"),
                            rs.getString("receipt_token")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving vote by receipt token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the participation status of a voter for a given election.
     *
     * @param voterId    unique identifier of the voter
     * @param electionId unique identifier of the election
     * @return {@link VoterElectionStatus} domain model or null if not found
     */
    public VoterElectionStatus getVoterElectionStatus(long voterId, long electionId) {
        String sql = "SELECT voter_id, election_id, has_voted, voted_at, receipt_token " +
                     "FROM voter_election_status " +
                     "WHERE voter_id = ? AND election_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, voterId);
            pstmt.setLong(2, electionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new VoterElectionStatus(
                            rs.getLong("voter_id"),
                            rs.getLong("election_id"),
                            rs.getBoolean("has_voted"),
                            rs.getTimestamp("voted_at"),
                            rs.getString("receipt_token")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching voter election status: " + e.getMessage());
        }
        return null;
    }

    /**
     * Generates a 64-character SHA-256 cryptographic receipt token from voter ID, election ID, timestamp, and a nonce.
     *
     * @param voterId    voter ID
     * @param electionId election ID
     * @return 64-character hex-encoded SHA-256 hash
     */
    public static String generateReceiptToken(long voterId, long electionId) {
        try {
            long timestamp = System.currentTimeMillis();
            String payload = voterId + ":" + electionId + ":" + timestamp + ":" + UUID.randomUUID().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 cryptographic algorithm not available in current JVM", e);
        }
    }
}
