package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.Candidate;
import com.ovs.models.CandidateResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link Candidate} entities in the MySQL database.
 * Handles candidate nomination, election association, and real-time ballot tallying.
 */
public class CandidateDAO {

    /**
     * Adds and persists a new candidate for a specified election.
     *
     * @param candidate candidate domain entity containing profile and election link
     * @return true if candidate was successfully created, false otherwise
     */
    public boolean addCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate entity cannot be null.");
        }

        String sql = "INSERT INTO candidates (name, party_symbol, manifesto, election_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, candidate.getName());
            pstmt.setString(2, candidate.getPartySymbol());
            pstmt.setString(3, candidate.getManifesto());
            pstmt.setLong(4, candidate.getElectionId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        candidate.setCandidateId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding candidate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all candidates registered under a specific election.
     *
     * @param electionId unique identifier of the target election
     * @return list of {@link Candidate} entities associated with the election
     */
    public List<Candidate> getCandidatesByElection(long electionId) {
        List<Candidate> candidates = new ArrayList<>();
        String sql = "SELECT candidate_id, name, party_symbol, manifesto, election_id " +
                     "FROM candidates " +
                     "WHERE election_id = ? " +
                     "ORDER BY candidate_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, electionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    candidates.add(mapResultSetToCandidate(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving candidates for election (" + electionId + "): " + e.getMessage());
        }
        return candidates;
    }

    /**
     * Retrieves a candidate by their unique primary key identifier.
     *
     * @param candidateId unique identifier of the candidate
     * @return {@link Candidate} entity or null if not found
     */
    public Candidate getCandidateById(long candidateId) {
        String sql = "SELECT candidate_id, name, party_symbol, manifesto, election_id " +
                     "FROM candidates " +
                     "WHERE candidate_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, candidateId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCandidate(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching candidate by ID (" + candidateId + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing candidate's profile information.
     *
     * @param candidate candidate domain model with updated details
     * @return true if updated, false otherwise
     */
    public boolean updateCandidate(Candidate candidate) {
        if (candidate == null) {
            return false;
        }

        String sql = "UPDATE candidates SET name = ?, party_symbol = ?, manifesto = ? WHERE candidate_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, candidate.getName());
            pstmt.setString(2, candidate.getPartySymbol());
            pstmt.setString(3, candidate.getManifesto());
            pstmt.setLong(4, candidate.getCandidateId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating candidate (" + candidate.getCandidateId() + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a candidate by primary key identifier.
     *
     * @param candidateId unique identifier of candidate to remove
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteCandidate(long candidateId) {
        String sql = "DELETE FROM candidates WHERE candidate_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, candidateId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting candidate (" + candidateId + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Tabulates aggregated vote results for all candidates participating in a specified election.
     *
     * @param electionId unique identifier of the election
     * @return list of {@link CandidateResult} DTO projections ordered by highest vote count
     */
    public List<CandidateResult> getCandidateResultsByElection(long electionId) {
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
            System.err.println("Error tabulating candidate results for election (" + electionId + "): " + e.getMessage());
        }
        return results;
    }

    /**
     * Maps the current row of a {@link ResultSet} into a {@link Candidate} domain model.
     *
     * @param rs active ResultSet positioned at a candidate row
     * @return populated {@link Candidate} object
     * @throws SQLException if a column reading error occurs
     */
    private Candidate mapResultSetToCandidate(ResultSet rs) throws SQLException {
        return new Candidate(
                rs.getLong("candidate_id"),
                rs.getString("name"),
                rs.getString("party_symbol"),
                rs.getString("manifesto"),
                rs.getLong("election_id"),
                "APPROVED"
        );
    }
}
