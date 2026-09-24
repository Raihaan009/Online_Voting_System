package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity representing a secret ballot record.
 */
public class Vote implements Serializable {

    private static final long serialVersionUID = 1L;

    private long voteId;
    private long electionId;
    private long candidateId;
    private Timestamp voteTimestamp;
    private String receiptToken;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public Vote() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param voteId        unique identifier of the cast vote
     * @param electionId    foreign key linking to the election
     * @param candidateId   foreign key linking to the selected candidate
     * @param voteTimestamp time at which the vote was committed
     * @param receiptToken  cryptographic hash receipt token for the voter
     */
    public Vote(long voteId, long electionId, long candidateId, Timestamp voteTimestamp, String receiptToken) {
        this.voteId = voteId;
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.voteTimestamp = voteTimestamp;
        this.receiptToken = receiptToken;
    }

    /**
     * Convenient constructor for casting a vote without auto-generated ID.
     *
     * @param electionId    foreign key linking to the election
     * @param candidateId   foreign key linking to the selected candidate
     * @param voteTimestamp time at which the vote was committed
     * @param receiptToken  cryptographic hash receipt token
     */
    public Vote(long electionId, long candidateId, Timestamp voteTimestamp, String receiptToken) {
        this.electionId = electionId;
        this.candidateId = candidateId;
        this.voteTimestamp = voteTimestamp;
        this.receiptToken = receiptToken;
    }

    // Getters and Setters

    public long getVoteId() {
        return voteId;
    }

    public void setVoteId(long voteId) {
        this.voteId = voteId;
    }

    public long getElectionId() {
        return electionId;
    }

    public void setElectionId(long electionId) {
        this.electionId = electionId;
    }

    public long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(long candidateId) {
        this.candidateId = candidateId;
    }

    public Timestamp getVoteTimestamp() {
        return voteTimestamp;
    }

    public void setVoteTimestamp(Timestamp voteTimestamp) {
        this.voteTimestamp = voteTimestamp;
    }

    public String getReceiptToken() {
        return receiptToken;
    }

    public void setReceiptToken(String receiptToken) {
        this.receiptToken = receiptToken;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "voteId=" + voteId +
                ", electionId=" + electionId +
                ", candidateId=" + candidateId +
                ", voteTimestamp=" + voteTimestamp +
                ", receiptToken='" + receiptToken + '\'' +
                '}';
    }
}
