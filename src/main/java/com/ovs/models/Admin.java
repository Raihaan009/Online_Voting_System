package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing an administrative user with governance privileges.
 */
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    private long adminId;
    private String name;
    private String email;
    private String passwordHash;
    private String role;
    private Timestamp createdAt;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public Admin() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param adminId      unique identifier of the administrator
     * @param name         full name of the administrator
     * @param email        administrative email address
     * @param passwordHash BCrypt hashed password
     * @param role         administrative role (e.g., ADMIN, SUPERADMIN)
     * @param createdAt    timestamp when admin account was created
     */
    public Admin(long adminId, String name, String email, String passwordHash, String role, Timestamp createdAt) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Convenient constructor for creating an administrator without ID and createdAt.
     *
     * @param name         full name of the administrator
     * @param email        administrative email address
     * @param passwordHash BCrypt hashed password
     * @param role         administrative role
     */
    public Admin(String name, String email, String passwordHash, String role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters

    public long getAdminId() {
        return adminId;
    }

    public void setAdminId(long adminId) {
        this.adminId = adminId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
