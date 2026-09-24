package com.ovs.util;

import com.ovs.dao.CandidateDAO;
import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoterDAO;
import com.ovs.models.Candidate;
import com.ovs.models.CandidateResult;
import com.ovs.models.Election;
import com.ovs.models.Voter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Standalone verification runner to comprehensively test and validate Phase 2 components:
 * 1. PasswordUtil (BCrypt hashing and verification)
 * 2. VoterDAO (Registration, authentication, and voter queries)
 * 3. ElectionDAO (Election creation, active window filtering, and lookup)
 * 4. CandidateDAO (Nomination registration, election lookups, and result tabulation)
 */
public class Phase2TestRunner {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);
        System.out.println(BOLD + CYAN + "            ONLINE VOTING SYSTEM - PHASE 2 ARCHITECTURE TEST RUNNER             " + RESET);
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);

        int passedCount = 0;
        int totalTests = 10;

        VoterDAO voterDAO = new VoterDAO();
        ElectionDAO electionDAO = new ElectionDAO();
        CandidateDAO candidateDAO = new CandidateDAO();

        // ---------------------------------------------------------------------
        // TEST 1: Direct BCrypt Password Hashing & Verification
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 1] Testing PasswordUtil (BCrypt 12-Round Hashing)..." + RESET);
        String rawPassword = "VoterSecurePass2026!";
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        System.out.println("  Plaintext Password : " + rawPassword);
        System.out.println("  BCrypt Generated   : " + hashedPassword);

        boolean directValid = PasswordUtil.checkPassword(rawPassword, hashedPassword);
        boolean directInvalid = !PasswordUtil.checkPassword("WrongSecretPass123", hashedPassword);
        boolean nullSafe = !PasswordUtil.checkPassword(null, hashedPassword) && !PasswordUtil.checkPassword(rawPassword, null);

        if (directValid && directInvalid && nullSafe && hashedPassword.startsWith("$2a$12$")) {
            System.out.println(GREEN + "  [PASSED] PasswordUtil BCrypt hashing & validation (valid, invalid, null-safe)." + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] PasswordUtil validation failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 2: Voter Registration with Automatic BCrypt Hashing
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 2] Testing VoterDAO.registerVoter()..." + RESET);
        String testEmail = "testvoter@college.edu";
        
        // Ensure idempotency by deleting any pre-existing record from past test runs
        voterDAO.deleteVoterByEmail(testEmail);

        Voter newVoter = new Voter();
        newVoter.setName("Alex Morgan");
        newVoter.setEmail(testEmail);
        newVoter.setPasswordHash(rawPassword); // Plaintext passed in; DAO must hash it
        newVoter.setHasVoted(false);
        newVoter.setStatus("APPROVED");

        boolean registered = voterDAO.registerVoter(newVoter);
        if (registered && newVoter.getVoterId() > 0) {
            System.out.println(GREEN + "  [PASSED] Voter successfully registered! Generated Voter ID: " + newVoter.getVoterId() + RESET);
            System.out.println("  Persisted Hash: " + newVoter.getPasswordHash());
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Voter registration failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 3: Voter Authentication with Valid Password
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 3] Testing VoterDAO.login() with VALID credentials..." + RESET);
        Voter authenticatedVoter = voterDAO.login(testEmail, rawPassword);
        if (authenticatedVoter != null && authenticatedVoter.getEmail().equalsIgnoreCase(testEmail)) {
            System.out.println(GREEN + "  [PASSED] Authentication succeeded for: " + authenticatedVoter.getName() + 
                               " (" + authenticatedVoter.getEmail() + ")" + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Valid voter authentication failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 4: Voter Authentication with INVALID Password
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 4] Testing VoterDAO.login() with INVALID credentials..." + RESET);
        Voter failedAuth = voterDAO.login(testEmail, "IncorrectPasswordXYZ");
        if (failedAuth == null) {
            System.out.println(GREEN + "  [PASSED] Authentication correctly rejected invalid password." + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Security violation: Login succeeded with incorrect password!" + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 5: Voter Retrieval by ID and Email
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 5] Testing VoterDAO.getVoterById() & getVoterByEmail()..." + RESET);
        Voter byId = voterDAO.getVoterById(newVoter.getVoterId());
        Voter byEmail = voterDAO.getVoterByEmail(testEmail);

        if (byId != null && byEmail != null && byId.getVoterId() == byEmail.getVoterId()) {
            System.out.println(GREEN + "  [PASSED] Successfully fetched voter by ID and Email." + RESET);
            System.out.println("  Record: " + byId);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Voter query by ID/Email failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 6: Election Creation
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 6] Testing ElectionDAO.createElection()..." + RESET);
        String timestampSuffix = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        Election testElection = new Election();
        testElection.setTitle("Campus Executive Council Election " + timestampSuffix);
        testElection.setDescription("Democratic election for Student Body President and Vice President representatives.");
        testElection.setStartDate(LocalDateTime.now().minusHours(1)); // Open 1 hour ago
        testElection.setEndDate(LocalDateTime.now().plusDays(3));      // Closes in 3 days
        testElection.setStatus("ACTIVE");

        boolean electionCreated = electionDAO.createElection(testElection);
        if (electionCreated && testElection.getElectionId() > 0) {
            System.out.println(GREEN + "  [PASSED] Election created! Generated Election ID: " + testElection.getElectionId() + RESET);
            System.out.println("  Title: " + testElection.getTitle());
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Election creation failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 7: Active Elections Filtering
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 7] Testing ElectionDAO.getActiveElections()..." + RESET);
        List<Election> activeElections = electionDAO.getActiveElections();
        boolean foundCreatedElection = activeElections.stream()
                .anyMatch(e -> e.getElectionId() == testElection.getElectionId());

        if (foundCreatedElection) {
            System.out.println(GREEN + "  [PASSED] Retrieved active elections list (Found " + activeElections.size() + 
                               " active election(s))." + RESET);
            System.out.println("  Active Election Detected: ID=" + testElection.getElectionId() + " Title=" + testElection.getTitle());
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Created active election not present in active elections list." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 8: Election Retrieval by ID
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 8] Testing ElectionDAO.getElectionById()..." + RESET);
        Election fetchedElection = electionDAO.getElectionById(testElection.getElectionId());
        if (fetchedElection != null && fetchedElection.getTitle().equals(testElection.getTitle())) {
            System.out.println(GREEN + "  [PASSED] Successfully fetched election by ID: " + fetchedElection.getElectionId() + RESET);
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Fetch election by ID failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 9: Adding Candidates to Election
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 9] Testing CandidateDAO.addCandidate()..." + RESET);
        Candidate candidate1 = new Candidate(
                "Samantha Reed",
                "Progressive Tech Alliance (💻)",
                "Pledging campus high-speed Wi-Fi, open-access research repositories, and modernized 24/7 study lounges.",
                testElection.getElectionId(),
                "APPROVED"
        );

        Candidate candidate2 = new Candidate(
                "David Chen",
                "Sustainable Campus Coalition (🌱)",
                "Committed to solar-powered campus transit, zero-single-use plastics, and transparent student government finances.",
                testElection.getElectionId(),
                "APPROVED"
        );

        boolean c1Added = candidateDAO.addCandidate(candidate1);
        boolean c2Added = candidateDAO.addCandidate(candidate2);

        if (c1Added && c2Added && candidate1.getCandidateId() > 0 && candidate2.getCandidateId() > 0) {
            System.out.println(GREEN + "  [PASSED] Successfully registered 2 candidates!" + RESET);
            System.out.println("  - Candidate #1: " + candidate1.getName() + " [ID: " + candidate1.getCandidateId() + "] Symbol: " + candidate1.getPartySymbol());
            System.out.println("  - Candidate #2: " + candidate2.getName() + " [ID: " + candidate2.getCandidateId() + "] Symbol: " + candidate2.getPartySymbol());
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Adding candidates failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // TEST 10: Fetch Candidates by Election & Candidate by ID
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + "[STEP 10] Testing CandidateDAO.getCandidatesByElection() & Tabulation..." + RESET);
        List<Candidate> electionCandidates = candidateDAO.getCandidatesByElection(testElection.getElectionId());
        Candidate singleCandidate = candidateDAO.getCandidateById(candidate1.getCandidateId());
        List<CandidateResult> results = candidateDAO.getCandidateResultsByElection(testElection.getElectionId());

        if (electionCandidates.size() >= 2 && singleCandidate != null && results.size() >= 2) {
            System.out.println(GREEN + "  [PASSED] Successfully retrieved candidates and tabulated initial results!" + RESET);
            System.out.println("  Candidate Count for Election " + testElection.getElectionId() + ": " + electionCandidates.size());
            System.out.println("  Initial Tabulation Results:");
            for (CandidateResult cr : results) {
                System.out.println("    * " + cr.getCandidateName() + " (" + cr.getPartySymbol() + ") -> Votes: " + cr.getTotalVotes());
            }
            passedCount++;
        } else {
            System.err.println(RED + "  [FAILED] Candidate querying or results tabulation failed." + RESET);
        }

        // ---------------------------------------------------------------------
        // FINAL SUMMARY REPORT
        // ---------------------------------------------------------------------
        System.out.println("\n" + BOLD + CYAN + "================================================================================" + RESET);
        System.out.println(BOLD + "                        PHASE 2 EXECUTION SUMMARY                               " + RESET);
        System.out.println(BOLD + CYAN + "================================================================================" + RESET);
        System.out.println("Total Verification Tests  : " + totalTests);
        System.out.println("Tests Passed              : " + (passedCount == totalTests ? (GREEN + passedCount + RESET) : (RED + passedCount + RESET)));
        System.out.println("Tests Failed              : " + (totalTests - passedCount == 0 ? (GREEN + "0" + RESET) : (RED + (totalTests - passedCount) + RESET)));
        
        if (passedCount == totalTests) {
            System.out.println(BOLD + GREEN + "\n>>> ALL PHASE 2 REQUIREMENTS SUCCESSFULLY IMPLEMENTED & VERIFIED! <<<" + RESET);
        } else {
            System.err.println(BOLD + RED + "\n>>> SOME TESTS FAILED. PLEASE REVIEW LOGS. <<<" + RESET);
            System.exit(1);
        }
    }
}
