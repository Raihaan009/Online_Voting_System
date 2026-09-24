package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain model representing an immutable administrative audit log entry.
 * Captures administrative actions, timestamps, and IP addresses for governance compliance.
 */
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private long logId;
    private String adminEmail;
    private String actionType;
    private String details;
    private String ipAddress;
    private Timestamp createdAt;

    /**
     * Default constructor.
     */
    public AuditLog() {
    }

    /**
     * Constructor without generated logId and createdAt timestamp (used for creation).
     *
     * @param adminEmail administrator institutional email
     * @param actionType categorized action (e.g., LOGIN, CREATE_ELECTION, NOMINATE_CANDIDATE)
     * @param details    detailed JSON/text description of the operation
     * @param ipAddress  client IP address
     */
    public AuditLog(String adminEmail, String actionType, String details, String ipAddress) {
        this.adminEmail = adminEmail;
        this.actionType = actionType;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    /**
     * Full constructor for retrieving audit logs from persistent storage.
     *
     * @param logId      unique audit log ID
     * @param adminEmail administrator institutional email
     * @param actionType categorized action
     * @param details    detailed description
     * @param ipAddress  client IP address
     * @param createdAt  creation timestamp
     */
    public AuditLog(long logId, String adminEmail, String actionType, String details, String ipAddress, Timestamp createdAt) {
        this.logId = logId;
        this.adminEmail = adminEmail;
        this.actionType = actionType;
        this.details = details;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "logId=" + logId +
                ", adminEmail='" + adminEmail + '\'' +
                ", actionType='" + actionType + '\'' +
                ", details='" + details + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
