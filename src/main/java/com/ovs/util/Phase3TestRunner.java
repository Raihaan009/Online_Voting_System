package com.ovs.util;

import com.ovs.config.DBConnection;
import com.ovs.controllers.AdminServlet;
import com.ovs.controllers.LoginServlet;
import com.ovs.controllers.LogoutServlet;
import com.ovs.controllers.RegisterServlet;
import com.ovs.controllers.VoteServlet;
import com.ovs.dao.CandidateDAO;
import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.dao.VoterDAO;
import com.ovs.filters.AuthenticationFilter;
import com.ovs.models.Candidate;
import com.ovs.models.CandidateResult;
import com.ovs.models.Election;
import com.ovs.models.Vote;
import com.ovs.models.Voter;
import com.ovs.models.VoterElectionStatus;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebServlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Standalone verification runner to comprehensively test and validate Phase 3 components:
 * 1. Transactional Voting Engine (ACID transactions, secret ballot privacy, duplicate vote prevention).
 * 2. Cryptographic SHA-256 Digital Receipt Generation & Lookup.
 * 3. Participation Status & Global Voter Flag Updates.
 * 4. Live Election Results Tabulation via VoteDAO.
 * 5. Security & Authentication Filter Annotation & Configuration.
 * 6. Jakarta EE 10 Controller Annotations & WebServlet Route Mapping.
 */
public class Phase3TestRunner {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);
        System.out.println(BOLD + CYAN + "            ONLINE VOTING SYSTEM - PHASE 3 ARCHITECTURE TEST RUNNER             " + RESET);
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);

        int passedCount = 0;
        int totalTests = 10;

        VoteDAO voteDAO = new VoteDAO();
        VoterDAO voterDAO = new VoterDAO();
        ElectionDAO electionDAO = new ElectionDAO();
        CandidateDAO candidateDAO = new CandidateDAO();

        // ---------------------------------------------------------------------
        // TEST 1: Database Connectivity & Driver Verification
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 1] Testing Database Connectivity & JDBC Driver..." + RESET);
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(GREEN + "  [PASSED] Database connected! Product: " + 
                                   conn.getMetaData().getDatabaseProductName() + " v" + 
                                   conn.getMetaData().getDatabaseProductVersion() + RESET);
                passedCount++;
            } else {
                System.err.println(RED + "  [FAILED] Connection obtained was null or closed." + RESET);
            }
        } catch (SQLException e) {
            System.err.println(RED + "  [FAILED] Database connection failed: " + e.getMessage() + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 2: Active Election & Competing Candidates Setup
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 2] Preparing Active Election & Competing Candidates..." + RESET);
        String suffix = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Election testElection = new Election();
        testElection.setTitle("Campus Presidential Election " + suffix);
        testElection.setDescription("Democratic election for Campus Student President and Executive Board.");
        testElection.setStartDate(LocalDateTime.now().minusHours(2)); // Open
        testElection.setEndDate(LocalDateTime.now().plusDays(5));       // Closes in 5 days
        testElection.setStatus("ACTIVE");

        boolean electionCreated = electionDAO.createElection(testElection);
        long electionId = testElection.getElectionId();

        Candidate candidateA = new Candidate("Elena Rostova", "Alliance for Innovation (⚡)", "Expanding student research grants.", electionId, "APPROVED");
        Candidate candidateB = new Candidate("Marcus Sterling", "Civic Leadership Party (🏛️)", "Campus sustainability & tuition reform.", electionId, "APPROVED");

        boolean c1Added = candidateDAO.addCandidate(candidateA);
        boolean c2Added = candidateDAO.addCandidate(candidateB);

        if (electionCreated && c1Added && c2Added && candidateA.getCandidateId() > 0 && candidateB.getCandidateId() > 0) {
            System.out.println(GREEN + "  [PASSED] Test Election and Candidates initialized successfully." + RESET);
            System.out.println("  Election ID: " + electionId + " | Title: " + testElection.getTitle());
            System.out.println("  - Candidate A: " + candidateA.getName() + " [ID: " + candidateA.getCandidateId() + "]");
            System.out.println("  - Candidate B: " + candidateB.getName() + " [ID: " + candidateB.getCandidateId() + "]");
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Failed to initialize test election or candidates." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 3: Eligible Voter Provisioning & Pre-Vote Verification
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 3] Provisioning Test Voter & Pre-Vote Status Check..." + RESET);
        String voterEmail = "p3_testvoter_" + suffix + "@college.edu";
        voterDAO.deleteVoterByEmail(voterEmail);

        Voter voter = new Voter();
        voter.setName("Jordan Lee");
        voter.setEmail(voterEmail);
        voter.setPasswordHash("VoterSecret2026!");
        voter.setHasVoted(false);
        voter.setStatus("APPROVED");

        boolean voterRegistered = voterDAO.registerVoter(voter);
        long voterId = voter.getVoterId();

        boolean initialVotedStatus = voteDAO.hasVoterVotedInElection(voterId, electionId);

        if (voterRegistered && voterId > 0 && !initialVotedStatus) {
            System.out.println(GREEN + "  [PASSED] Voter successfully provisioned (ID: " + voterId + ") with has_voted = false." + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Voter setup or initial vote check failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 4: Transactional Secret Ballot Casting (ACID Transaction)
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 4] Executing Transactional Vote via VoteDAO.castVote()..." + RESET);
        String receiptToken = null;
        try {
            receiptToken = voteDAO.castVote(voterId, electionId, candidateA.getCandidateId());
        } catch (SQLException e) {
            System.err.println(RED + "  [ERROR] castVote threw unexpected exception: " + e.getMessage() + RESET);
        }

        boolean validReceipt = receiptToken != null && receiptToken.length() == 64 && receiptToken.matches("^[a-f0-9]{64}$");
        VoterElectionStatus statusAfterVote = voteDAO.getVoterElectionStatus(voterId, electionId);
        Voter voterRecordAfter = voterDAO.getVoterById(voterId);

        if (validReceipt && statusAfterVote != null && statusAfterVote.isHasVoted() &&
            receiptToken.equals(statusAfterVote.getReceiptToken()) &&
            voterRecordAfter != null && voterRecordAfter.isHasVoted()) {

            System.out.println(GREEN + "  [PASSED] Atomic transaction committed successfully!" + RESET);
            System.out.println("  Cryptographic Ballot Receipt Token (SHA-256): " + receiptToken);
            System.out.println("  Voter Election Status: has_voted = " + statusAfterVote.isHasVoted() + 
                               " | voted_at = " + statusAfterVote.getVotedAt());
            System.out.println("  Global Voter Record  : has_voted = " + voterRecordAfter.isHasVoted());
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Transaction verification failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 5: Duplicate Voting Prevention Assertion
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 5] Testing Duplicate Voting Enforcement (One-Person-One-Vote)..." + RESET);
        boolean duplicateBlocked = false;
        try {
            // Attempt to vote a second time in the same election for Candidate B
            voteDAO.castVote(voterId, electionId, candidateB.getCandidateId());
        } catch (SQLException e) {
            duplicateBlocked = true;
            System.out.println("  Caught expected exception: " + YELLOW + e.getMessage() + RESET);
        }

        if (duplicateBlocked) {
            System.out.println(GREEN + "  [PASSED] Duplicate vote attempt correctly detected and blocked with rollback!" + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Security vulnerability: Duplicate vote succeeded!" + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 6: Cryptographic Ballot Receipt Verification
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 6] Testing Cryptographic Receipt Lookup via VoteDAO..." + RESET);
        Vote recordedVote = voteDAO.getVoteByReceiptToken(receiptToken);

        if (recordedVote != null && 
            recordedVote.getElectionId() == electionId && 
            recordedVote.getCandidateId() == candidateA.getCandidateId() &&
            receiptToken.equals(recordedVote.getReceiptToken())) {

            System.out.println(GREEN + "  [PASSED] Secret ballot successfully verified by receipt token." + RESET);
            System.out.println("  Retrieved Ballot: Vote ID=" + recordedVote.getVoteId() + 
                               " | Election ID=" + recordedVote.getElectionId() + 
                               " | Candidate ID=" + recordedVote.getCandidateId() + 
                               " | Timestamp=" + recordedVote.getVoteTimestamp());
            System.out.println("  Note: Secret ballot preserves voter privacy (voter_id is strictly omitted in votes table).");
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Ballot receipt verification lookup failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 7: Tabulating Live Election Results via VoteDAO.getElectionResults()
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 7] Tabulating Live Election Results (GROUP BY candidate_id)..." + RESET);
        List<CandidateResult> results = voteDAO.getElectionResults(electionId);

        boolean resultsValid = results != null && !results.isEmpty();
        long candidateAVotes = 0;
        long candidateBVotes = 0;

        if (resultsValid) {
            System.out.println("  Tabulated Standings for Election ID " + electionId + ":");
            for (CandidateResult cr : results) {
                System.out.println("    * " + cr.getCandidateName() + " (" + cr.getPartySymbol() + ") -> Total Votes: " + cr.getTotalVotes());
                if (cr.getCandidateId() == candidateA.getCandidateId()) {
                    candidateAVotes = cr.getTotalVotes();
                } else if (cr.getCandidateId() == candidateB.getCandidateId()) {
                    candidateBVotes = cr.getTotalVotes();
                }
            }
        }

        if (resultsValid && candidateAVotes == 1 && candidateBVotes == 0) {
            System.out.println(GREEN + "  [PASSED] Results aggregation accurately reflects cast ballot (Candidate A: 1, Candidate B: 0)." + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Election results tabulation discrepancy." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 8: Security Filter (@WebFilter) Inspection
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 8] Validating AuthenticationFilter Architecture..." + RESET);
        Class<AuthenticationFilter> filterClass = AuthenticationFilter.class;
        WebFilter webFilterAnn = filterClass.getAnnotation(WebFilter.class);

        boolean filterImplementsInterface = jakarta.servlet.Filter.class.isAssignableFrom(filterClass);
        boolean filterHasAnnotation = webFilterAnn != null;
        List<String> patterns = filterHasAnnotation ? Arrays.asList(webFilterAnn.urlPatterns()) : List.of();
        boolean hasRequiredPatterns = patterns.contains("/voter/*") && patterns.contains("/admin/*");

        if (filterImplementsInterface && filterHasAnnotation && hasRequiredPatterns) {
            System.out.println(GREEN + "  [PASSED] AuthenticationFilter complies with Jakarta Servlet 6.0 standards." + RESET);
            System.out.println("  Configured URL Patterns: " + patterns);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] AuthenticationFilter configuration is incomplete or missing annotations." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 9: Servlets / Controllers (@WebServlet) Verification
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 9] Validating Jakarta EE 10 Servlets & Routing Architecture..." + RESET);
        WebServlet regAnn = RegisterServlet.class.getAnnotation(WebServlet.class);
        WebServlet logAnn = LoginServlet.class.getAnnotation(WebServlet.class);
        WebServlet logoutAnn = LogoutServlet.class.getAnnotation(WebServlet.class);
        WebServlet voteAnn = VoteServlet.class.getAnnotation(WebServlet.class);
        WebServlet adminAnn = AdminServlet.class.getAnnotation(WebServlet.class);

        boolean regOk = regAnn != null && Arrays.asList(regAnn.value()).contains("/register");
        boolean logOk = logAnn != null && Arrays.asList(logAnn.value()).contains("/login");
        boolean logoutOk = logoutAnn != null && Arrays.asList(logoutAnn.value()).contains("/logout");
        boolean voteOk = voteAnn != null && Arrays.asList(voteAnn.value()).contains("/voter/cast-vote");
        boolean adminOk = adminAnn != null && Arrays.asList(adminAnn.value()).contains("/admin/dashboard");

        if (regOk && logOk && logoutOk && voteOk && adminOk) {
            System.out.println(GREEN + "  [PASSED] All 5 required Servlets verified with valid @WebServlet bindings:" + RESET);
            System.out.println("    - RegisterServlet : " + Arrays.toString(regAnn.value()));
            System.out.println("    - LoginServlet    : " + Arrays.toString(logAnn.value()));
            System.out.println("    - LogoutServlet   : " + Arrays.toString(logoutAnn.value()));
            System.out.println("    - VoteServlet     : " + Arrays.toString(voteAnn.value()));
            System.out.println("    - AdminServlet    : " + Arrays.toString(adminAnn.value()));
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] One or more servlets missing required @WebServlet mappings." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 10: Clean Audit Status & Cryptographic Integrity Check
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 10] Testing SHA-256 Token Entropy & Uniqueness..." + RESET);
        String token1 = VoteDAO.generateReceiptToken(voterId, electionId);
        String token2 = VoteDAO.generateReceiptToken(voterId, electionId);

        boolean tokensDifferent = !token1.equals(token2);
        boolean lengthsMatch = token1.length() == 64 && token2.length() == 64;

        if (tokensDifferent && lengthsMatch) {
            System.out.println(GREEN + "  [PASSED] Cryptographic token generation exhibits unique entropy per invocation." + RESET);
            System.out.println("  Sample Token 1: " + token1);
            System.out.println("  Sample Token 2: " + token2);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Token uniqueness test failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // FINAL SUMMARY REPORT
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + CYAN + "================================================================================" + RESET);
        System.out.println(BOLD + "                        PHASE 3 EXECUTION SUMMARY                               " + RESET);
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);
        System.out.println("Total Verification Tests  : " + totalTests);
        System.out.println("Tests Passed              : " + (passedCount == totalTests ? (GREEN + passedCount + RESET) : (RED + passedCount + RESET)));
        System.out.println("Tests Failed              : " + (totalTests - passedCount == 0 ? (GREEN + "0" + RESET) : (RED + (totalTests - passedCount) + RESET)));

        if (passedCount == totalTests) {
            System.out.println(BOLD + GREEN + "\n>>> ALL PHASE 3 REQUIREMENTS SUCCESSFULLY IMPLEMENTED & VERIFIED! <<<" + RESET);
        } else {
            System.err.println(BOLD + RED + "\n>>> SOME TESTS FAILED. PLEASE REVIEW LOGS. <<<" + RESET);
            System.exit(1);
        }
    }
}
