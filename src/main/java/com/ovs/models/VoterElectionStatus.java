package com.ovs.models;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Domain entity tracking a voter's participation status within a specific election.
 */
public class VoterElectionStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private long voterId;
    private long electionId;
    private boolean hasVoted;
    private Timestamp votedAt;
    private String receiptToken;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public VoterElectionStatus() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param voterId      unique identifier of the voter
     * @param electionId   unique identifier of the election
     * @param hasVoted     boolean flag indicating whether the voter has voted in this election
     * @param votedAt      timestamp when the vote was recorded
     * @param receiptToken receipt reference hash token given to voter
     */
    public VoterElectionStatus(long voterId, long electionId, boolean hasVoted, Timestamp votedAt, String receiptToken) {
        this.voterId = voterId;
        this.electionId = electionId;
        this.hasVoted = hasVoted;
        this.votedAt = votedAt;
        this.receiptToken = receiptToken;
    }

    // Getters and Setters

    public long getVoterId() {
        return voterId;
    }

    public void setVoterId(long voterId) {
        this.voterId = voterId;
    }

    public long getElectionId() {
        return electionId;
    }

    public void setElectionId(long electionId) {
        this.electionId = electionId;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public Timestamp getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Timestamp votedAt) {
        this.votedAt = votedAt;
    }

    public String getReceiptToken() {
        return receiptToken;
    }

    public void setReceiptToken(String receiptToken) {
        this.receiptToken = receiptToken;
    }

    @Override
    public String toString() {
        return "VoterElectionStatus{" +
                "voterId=" + voterId +
                ", electionId=" + electionId +
                ", hasVoted=" + hasVoted +
                ", votedAt=" + votedAt +
                ", receiptToken='" + receiptToken + '\'' +
                '}';
    }
}
