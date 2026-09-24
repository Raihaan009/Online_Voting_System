package com.ovs.models;

import java.io.Serializable;

/**
 * Domain projection / DTO representing tabulated vote results for a candidate in an election.
 */
public class CandidateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private long candidateId;
    private String candidateName;
    private String partySymbol;
    private long electionId;
    private long totalVotes;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public CandidateResult() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param candidateId   unique identifier of the candidate
     * @param candidateName full name of the candidate
     * @param partySymbol   symbol or party name
     * @param electionId    unique identifier of the election
     * @param totalVotes    aggregated count of valid votes received
     */
    public CandidateResult(long candidateId, String candidateName, String partySymbol, long electionId, long totalVotes) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.partySymbol = partySymbol;
        this.electionId = electionId;
        this.totalVotes = totalVotes;
    }

    // Getters and Setters

    public long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getPartySymbol() {
        return partySymbol;
    }

    public void setPartySymbol(String partySymbol) {
        this.partySymbol = partySymbol;
    }

    public long getElectionId() {
        return electionId;
    }

    public void setElectionId(long electionId) {
        this.electionId = electionId;
    }

    public long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(long totalVotes) {
        this.totalVotes = totalVotes;
    }

    @Override
    public String toString() {
        return "CandidateResult{" +
                "candidateId=" + candidateId +
                ", candidateName='" + candidateName + '\'' +
                ", partySymbol='" + partySymbol + '\'' +
                ", electionId=" + electionId +
                ", totalVotes=" + totalVotes +
                '}';
    }
}
