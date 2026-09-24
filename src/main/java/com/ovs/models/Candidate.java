package com.ovs.models;

import java.io.Serializable;

/**
 * Domain entity representing an election candidate or nominee.
 */
public class Candidate implements Serializable {

    private static final long serialVersionUID = 1L;

    private long candidateId;
    private String name;
    private String partySymbol;
    private String manifesto;
    private long electionId;
    private String status;

    /**
     * Zero-argument constructor (Standard JavaBean requirement).
     */
    public Candidate() {
    }

    /**
     * Full-arguments constructor.
     *
     * @param candidateId unique identifier of the candidate
     * @param name        full name of the candidate
     * @param partySymbol party affiliation or ballot symbol representation
     * @param manifesto   electoral manifesto or candidate statement
     * @param electionId  foreign key linking to associated election
     * @param status      candidacy status (e.g., APPROVED, PENDING, WITHDRAWN)
     */
    public Candidate(long candidateId, String name, String partySymbol, String manifesto, long electionId, String status) {
        this.candidateId = candidateId;
        this.name = name;
        this.partySymbol = partySymbol;
        this.manifesto = manifesto;
        this.electionId = electionId;
        this.status = status;
    }

    /**
     * Convenient constructor for adding new candidates without ID.
     *
     * @param name        full name of the candidate
     * @param partySymbol party affiliation or ballot symbol representation
     * @param manifesto   electoral manifesto or candidate statement
     * @param electionId  foreign key linking to associated election
     * @param status      candidacy status
     */
    public Candidate(String name, String partySymbol, String manifesto, long electionId, String status) {
        this.name = name;
        this.partySymbol = partySymbol;
        this.manifesto = manifesto;
        this.electionId = electionId;
        this.status = status;
    }

    // Getters and Setters

    public long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(long candidateId) {
        this.candidateId = candidateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartySymbol() {
        return partySymbol;
    }

    public void setPartySymbol(String partySymbol) {
        this.partySymbol = partySymbol;
    }

    public String getManifesto() {
        return manifesto;
    }

    public void setManifesto(String manifesto) {
        this.manifesto = manifesto;
    }

    public long getElectionId() {
        return electionId;
    }

    public void setElectionId(long electionId) {
        this.electionId = electionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "candidateId=" + candidateId +
                ", name='" + name + '\'' +
                ", partySymbol='" + partySymbol + '\'' +
                ", manifesto='" + manifesto + '\'' +
                ", electionId=" + electionId +
                ", status='" + status + '\'' +
                '}';
    }
}
