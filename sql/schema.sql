-- =====================================================================
-- Online Voting System - Database Schema & Initial Data
-- Target Database Engine: MySQL 8.0+
-- Database Name: online_voting_db
-- =====================================================================

CREATE DATABASE IF NOT EXISTS online_voting_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE online_voting_db;

-- 1. Table: users (Voters and Administrators)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    voter_id_card VARCHAR(50) NOT NULL UNIQUE COMMENT 'National Voter ID or Student/Employee Unique ID',
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt salted hash',
    role ENUM('ADMIN', 'VOTER') NOT NULL DEFAULT 'VOTER',
    has_voted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Global or current election status flag',
    status ENUM('PENDING', 'ACTIVE', 'BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_voter_card (voter_id_card),
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table: elections
CREATE TABLE IF NOT EXISTS elections (
    election_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('UPCOMING', 'ACTIVE', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_election_status (status),
    INDEX idx_election_window (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table: candidates
CREATE TABLE IF NOT EXISTS candidates (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    election_id INT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    party_name VARCHAR(100) NOT NULL,
    symbol_name VARCHAR(50),
    symbol_url VARCHAR(255),
    photo_url VARCHAR(255),
    manifesto TEXT,
    vote_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_election FOREIGN KEY (election_id)
        REFERENCES elections (election_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_candidate_election (election_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table: votes (Ballot Box & Audit Trail)
CREATE TABLE IF NOT EXISTS votes (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    election_id INT NOT NULL,
    candidate_id INT NOT NULL,
    voter_id INT NOT NULL,
    cast_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ballot_hash VARCHAR(128) COMMENT 'Cryptographic hash ensuring vote immutability',
    -- Enforce one vote per voter per election
    CONSTRAINT uk_voter_election UNIQUE (election_id, voter_id),
    CONSTRAINT fk_vote_election FOREIGN KEY (election_id)
        REFERENCES elections (election_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_vote_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidates (candidate_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_vote_voter FOREIGN KEY (voter_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_vote_election (election_id),
    INDEX idx_vote_candidate (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Seed Data for Development and Testing
-- =====================================================================

-- Default Administrator account
-- Password: "AdminPassword123!" (BCrypt hashed with $2a$10$)
INSERT INTO users (voter_id_card, full_name, email, phone, password_hash, role, status)
VALUES (
    'ADMIN-001',
    'System Administrator',
    'admin@voting.system',
    '+1000000000',
    '$2a$10$e8k6mXN13aYpL38k8cI3e.n8R5fXFhI9Vl6q59hK9R3B0JgS4v2WW',
    'ADMIN',
    'ACTIVE'
) ON DUPLICATE KEY UPDATE user_id=user_id;

-- Sample Active Election
INSERT INTO elections (election_id, title, description, start_time, end_time, status)
VALUES (
    1,
    'General Council Election 2026',
    'Annual democratic election for Executive Council Representatives.',
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 7 DAY,
    'ACTIVE'
) ON DUPLICATE KEY UPDATE election_id=election_id;

-- Sample Candidates for Election #1
INSERT INTO candidates (candidate_id, election_id, full_name, party_name, symbol_name, manifesto, vote_count)
VALUES 
(
    1,
    1,
    'Alice Johnson',
    'Progressive Alliance',
    'Torch',
    'Committed to transparency, digital modernization, and student welfare.',
    0
),
(
    2,
    1,
    'Bob Martinez',
    'United Reform Coalition',
    'Eagle',
    'Focusing on institutional governance, resource efficiency, and sustainable community development.',
    0
) ON DUPLICATE KEY UPDATE candidate_id=candidate_id;
