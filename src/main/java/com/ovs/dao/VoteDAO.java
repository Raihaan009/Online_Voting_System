package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.Vote;
import com.ovs.models.VoterElectionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object (DAO) for managing secret ballots, voting receipts, and voter participation status.
 * Executes atomic transactions ensuring one-person-one-vote rules and cryptographic verification.
 */
public class VoteDAO {

    /**
     * Casts an immutable secret ballot within an ACID transaction.
     * Enforces the one-person-one-vote constraint by:
     * 1. Verifying voter has not already cast a ballot in the election.
     * 2. Committing the anonymous secret ballot into the {@code votes} table.
     * 3. Recording participation in {@code voter_election_status}.
     * 4. Updating the global {@code has_voted} flag on the voter record.
     *
     * @param voterId      unique identifier of the voter casting the ballot
     * @param electionId   target election ID
     * @param candidateId  selected candidate ID
     * @param receiptToken 64-character SHA-256 cryptographic receipt reference
     * @return true if vote was successfully committed, false if duplicate or database error
     */
    public boolean castVote(long voterId, long electionId, long candidateId, String receiptToken) {
        if (receiptToken == null || receiptToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Cryptographic receipt token cannot be null or empty.");
        }

        String checkStatusSql = "SELECT has_voted FROM voter_election_status WHERE voter_id = ? AND election_id = ?";
        String insertVoteSql = "INSERT INTO votes (election_id, candidate_id, vote_timestamp, receipt_token) VALUES (?, ?, NOW(), ?)";
        String recordStatusSql = "INSERT INTO voter_election_status (voter_id, election_id, has_voted, voted_at, receipt_token) " +
                                 "VALUES (?, ?, 1, NOW(), ?) " +
                                 "ON DUPLICATE KEY UPDATE has_voted = 1, voted_at = NOW(), receipt_token = VALUES(receipt_token)";
        String updateVoterFlagSql = "UPDATE voters SET has_voted = 1 WHERE voter_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID transaction

            // 1. Verify eligibility (Double-voting prevention)
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStatusSql)) {
                checkStmt.setLong(1, voterId);
                checkStmt.setLong(2, electionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getBoolean("has_voted")) {
                        conn.rollback();
                        return false; // Already voted in this election
                    }
                }
            }

            // 2. Insert ballot into votes table (decoupled from voter identity for ballot secrecy)
            try (PreparedStatement voteStmt = conn.prepareStatement(insertVoteSql, Statement.RETURN_GENERATED_KEYS)) {
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
}
