package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing an eligible institutional voter.
 */
public class Voter implements Serializable {

    private static final long serialVersionUID = 1L;

    private long voterId;
    private String name;
    private String email;
    private String passwordHash;
    private boolean hasVoted;
    private String status;
    private String department = "Computer Science";
    private Timestamp createdAt;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public Voter() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param voterId      unique identifier of the voter
     * @param name         full name of the voter
     * @param email        institutional email address
     * @param passwordHash BCrypt hashed password
     * @param hasVoted     flag indicating if the voter has cast a vote
     * @param status       account status (e.g., ACTIVE, PENDING, BLOCKED)
     * @param createdAt    timestamp when voter account was created
     */
    public Voter(long voterId, String name, String email, String passwordHash, boolean hasVoted, String status, Timestamp createdAt) {
        this.voterId = voterId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.hasVoted = hasVoted;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Convenient constructor for registration (without ID and createdAt).
     *
     * @param name         full name of the voter
     * @param email        institutional email address
     * @param passwordHash BCrypt hashed or plain password prior to registration
     * @param hasVoted     initial vote status
     * @param status       initial account status
     */
    public Voter(String name, String email, String passwordHash, boolean hasVoted, String status) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.hasVoted = hasVoted;
        this.status = status;
    }

    // Getters and Setters

    public long getVoterId() {
        return voterId;
    }

    public void setVoterId(long voterId) {
        this.voterId = voterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartment() {
        return department != null ? department : "Computer Science";
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Voter{" +
                "voterId=" + voterId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", hasVoted=" + hasVoted +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
