package com.ovs.dao;

import com.ovs.config.DBConnection;
import com.ovs.models.Election;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for managing {@link Election} entities in the MySQL database.
 * Encapsulates election lifecycle management and active polling window filtering.
 */
public class ElectionDAO {

    /**
     * Creates and persists a new election in the database.
     *
     * @param election domain model containing election configuration
     * @return true if election was successfully created, false otherwise
     */
    public boolean createElection(Election election) {
        if (election == null) {
            throw new IllegalArgumentException("Election entity cannot be null.");
        }

        String sql = "INSERT INTO elections (title, description, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, election.getTitle());
            pstmt.setString(2, election.getDescription());
            pstmt.setTimestamp(3, election.getStartDate() != null ? Timestamp.valueOf(election.getStartDate()) : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(4, election.getEndDate() != null ? Timestamp.valueOf(election.getEndDate()) : Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
            pstmt.setString(5, election.getStatus() != null ? election.getStatus() : "ACTIVE");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        election.setElectionId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error creating election: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all elections that are currently active and open for voting.
     * Evaluates elections where status = 'ACTIVE' and current system time is strictly
     * between the election's start_date and end_date.
     *
     * @return list of active {@link Election} instances
     */
    public List<Election> getActiveElections() {
        List<Election> activeElections = new ArrayList<>();
        String sql = "SELECT election_id, title, description, start_date, end_date, status, created_at " +
                     "FROM elections " +
                     "WHERE status = 'ACTIVE' AND NOW() BETWEEN start_date AND end_date " +
                     "ORDER BY start_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                activeElections.add(mapResultSetToElection(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving active elections: " + e.getMessage());
        }
        return activeElections;
    }

    /**
     * Retrieves a single election by its unique identifier.
     *
     * @param electionId primary key identifier of the election
     * @return {@link Election} entity or null if not found
     */
    public Election getElectionById(long electionId) {
        String sql = "SELECT election_id, title, description, start_date, end_date, status, created_at " +
                     "FROM elections " +
                     "WHERE election_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, electionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToElection(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching election by ID (" + electionId + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all elections regardless of status or polling window.
     *
     * @return list of all recorded {@link Election} instances
     */
    public List<Election> getAllElections() {
        List<Election> elections = new ArrayList<>();
        String sql = "SELECT election_id, title, description, start_date, end_date, status, created_at " +
                     "FROM elections ORDER BY election_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                elections.add(mapResultSetToElection(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all elections: " + e.getMessage());
        }
        return elections;
    }

    /**
     * Updates the status of an existing election (e.g. DRAFT, SCHEDULED, ACTIVE, CLOSED, PUBLISHED).
     *
     * @param electionId unique identifier of the election
     * @param status     new election status
     * @return true if updated, false otherwise
     */
    public boolean updateElectionStatus(long electionId, String status) {
        String sql = "UPDATE elections SET status = ? WHERE election_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setLong(2, electionId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating election status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an election and cascades removal of associated candidates and votes.
     *
     * @param electionId unique identifier of the election to delete
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteElection(long electionId) {
        String sql = "DELETE FROM elections WHERE election_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, electionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting election by ID (" + electionId + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Maps the current row of a {@link ResultSet} into an {@link Election} domain model.
     *
     * @param rs active ResultSet positioned on an election record
     * @return mapped {@link Election} entity
     * @throws SQLException if a column reading error occurs
     */
    private Election mapResultSetToElection(ResultSet rs) throws SQLException {
        Timestamp startTimestamp = rs.getTimestamp("start_date");
        LocalDateTime startDate = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;

        Timestamp endTimestamp = rs.getTimestamp("end_date");
        LocalDateTime endDate = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        return new Election(
                rs.getLong("election_id"),
                rs.getString("title"),
                rs.getString("description"),
                startDate,
                endDate,
                rs.getString("status"),
                rs.getTimestamp("created_at")
        );
    }
}
