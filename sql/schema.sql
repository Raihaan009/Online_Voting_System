-- =====================================================================
-- Online Voting System - Database Schema & Initial Data
-- Target Database Engine: MySQL 8.0+
-- Database Name: online_voting_db
-- Architecture: 3-Tier Enterprise MVC (Normalized Relational Model)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS online_voting_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_voting_db;

-- 1. Table: admin (System Governance & Election Administrators)
CREATE TABLE IF NOT EXISTS admin (
    admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 12-round salted hash',
    role ENUM('SUPER_ADMIN', 'ELECTION_ADMIN') NOT NULL DEFAULT 'ELECTION_ADMIN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_id),
    UNIQUE KEY uq_admin_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table: voters (Eligible Student / Institutional Voters)
CREATE TABLE IF NOT EXISTS voters (
    voter_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 12-round salted hash',
    has_voted TINYINT(1) NOT NULL DEFAULT 0,
    status ENUM('PENDING', 'APPROVED', 'SUSPENDED', 'REJECTED') NOT NULL DEFAULT 'APPROVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (voter_id),
    UNIQUE KEY uq_voter_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table: elections (Democratic Polling Events)
CREATE TABLE IF NOT EXISTS elections (
    election_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status ENUM('DRAFT', 'SCHEDULED', 'ACTIVE', 'CLOSED', 'PUBLISHED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (election_id),
    INDEX idx_election_window (status, start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table: candidates (Nominees Associated with Elections)
CREATE TABLE IF NOT EXISTS candidates (
    candidate_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    party_symbol VARCHAR(255) DEFAULT NULL,
    manifesto TEXT,
    election_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (candidate_id),
    KEY idx_candidate_election (election_id),
    CONSTRAINT fk_candidate_election FOREIGN KEY (election_id)
        REFERENCES elections (election_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Table: votes (Secret Ballot Box Decoupled from Voter Identity)
CREATE TABLE IF NOT EXISTS votes (
    vote_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    election_id BIGINT UNSIGNED NOT NULL,
    candidate_id BIGINT UNSIGNED NOT NULL,
    vote_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    receipt_token CHAR(64) NOT NULL COMMENT 'SHA-256 cryptographic ballot verification token',
    PRIMARY KEY (vote_id),
    UNIQUE KEY uq_vote_receipt (receipt_token),
    KEY idx_vote_election (election_id),
    KEY idx_vote_candidate (candidate_id),
    CONSTRAINT fk_vote_election FOREIGN KEY (election_id)
        REFERENCES elections (election_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vote_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidates (candidate_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Table: voter_election_status (Enforces One-Person-One-Vote per Election)
CREATE TABLE IF NOT EXISTS voter_election_status (
    voter_id BIGINT UNSIGNED NOT NULL,
    election_id BIGINT UNSIGNED NOT NULL,
    has_voted TINYINT(1) NOT NULL DEFAULT 0,
    voted_at DATETIME DEFAULT NULL,
    receipt_token CHAR(64) DEFAULT NULL,
    PRIMARY KEY (voter_id, election_id),
    UNIQUE KEY uq_status_receipt (receipt_token),
    KEY idx_status_election (election_id),
    CONSTRAINT fk_status_voter FOREIGN KEY (voter_id)
        REFERENCES voters (voter_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_status_election FOREIGN KEY (election_id)
        REFERENCES elections (election_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Seed Data for Development and Testing
-- =====================================================================

-- Default Super Administrator (Password: "AdminSecurePass2026!")
INSERT INTO admin (name, email, password_hash, role)
VALUES (
    'System Administrator',
    'admin@college.edu',
    '$2a$12$e8k6mXN13aYpL38k8cI3e.n8R5fXFhI9Vl6q59hK9R3B0JgS4v2WW',
    'SUPER_ADMIN'
) ON DUPLICATE KEY UPDATE name=name;

-- Sample Active Election
INSERT INTO elections (election_id, title, description, start_date, end_date, status)
VALUES (
    1,
    'Student Council General Election 2026',
    'Annual democratic election for Executive Council Representatives.',
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 7 DAY,
    'ACTIVE'
) ON DUPLICATE KEY UPDATE election_id=election_id;

-- Sample Candidates for Election #1
INSERT INTO candidates (candidate_id, name, party_symbol, manifesto, election_id)
VALUES 
(
    1,
    'Samantha Reed',
    'Progressive Tech Alliance (💻)',
    'Pledging high-speed campus Wi-Fi, 24/7 study lounges, and modern digital student services.',
    1
),
(
    2,
    'David Chen',
    'Sustainable Campus Coalition (🌱)',
    'Committed to solar-powered campus transit, zero-single-use plastics, and transparent financing.',
    1
) ON DUPLICATE KEY UPDATE candidate_id=candidate_id;

-- 7. Table: audit_logs (Administrative Governance & Operations Audit Trail)
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    admin_email VARCHAR(150) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    KEY idx_audit_admin (admin_email),
    KEY idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
