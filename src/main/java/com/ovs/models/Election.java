package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Domain entity representing an election or democratic polling event.
 */
public class Election implements Serializable {

    private static final long serialVersionUID = 1L;

    private long electionId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Timestamp createdAt;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public Election() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param electionId  unique identifier of the election
     * @param title       title/name of the election
     * @param description brief description of the election purpose
     * @param startDate   start date and time of voting
     * @param endDate     closing date and time of voting
     * @param status      election status (e.g., UPCOMING, ACTIVE, COMPLETED, ARCHIVED)
     * @param createdAt   timestamp of creation
     */
    public Election(long electionId, String title, String description, LocalDateTime startDate, LocalDateTime endDate, String status, Timestamp createdAt) {
        this.electionId = electionId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Convenient constructor for creating new elections without generated fields.
     *
     * @param title       title/name of the election
     * @param description brief description of the election purpose
     * @param startDate   start date and time of voting
     * @param endDate     closing date and time of voting
     * @param status      election status
     */
    public Election(String title, String description, LocalDateTime startDate, LocalDateTime endDate, String status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Getters and Setters

    public long getElectionId() {
        return electionId;
    }

    public void setElectionId(long electionId) {
        this.electionId = electionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * Helper getter returning epoch milliseconds for client-side JavaScript countdown timers.
     * Prevents JSP EL PropertyNotFoundException on LocalDateTime.
     *
     * @return epoch timestamp in milliseconds, or 0 if endDate is null
     */
    public long getEndTimeMillis() {
        if (endDate != null) {
            return endDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return 0L;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Election{" +
                "electionId=" + electionId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
