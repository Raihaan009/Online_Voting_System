/**
 * CampusVote - Client Utility & Interaction Script
 * Enterprise 3-Tier MVC Architecture Client Script
 * Handles Theme Toggling (Persistence), Password Matching, Ballot Modal, Double-Submit, & Audit Breakdowns
 */

// Immediate theme execution to prevent light-mode flickering on page load
(function() {
  const savedTheme = localStorage.getItem('campusvote_theme') || localStorage.getItem('theme') || 'light';
  document.documentElement.setAttribute('data-theme', savedTheme);
  updateIcon(savedTheme);
})();

function updateIcon(theme) {
  const icon = document.getElementById('themeIcon');
  if (icon) {
    icon.textContent = theme === 'dark' ? '☀️' : '🌙';
  }
}

document.addEventListener('DOMContentLoaded', () => {
    // -------------------------------------------------------------------------
    // 1. Global Light / Dark Mode Toggle & Persistence
    // -------------------------------------------------------------------------
    const savedTheme = localStorage.getItem('campusvote_theme') || localStorage.getItem('theme') || 'light';
    updateIcon(savedTheme);

    const toggleBtn = document.getElementById('themeToggleBtn');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('campusvote_theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateIcon(newTheme);
        });
    }

    // -------------------------------------------------------------------------
    // 2. Live Client Timestamp Synchronization
    // -------------------------------------------------------------------------
    const timeElements = document.querySelectorAll("#client-timestamp");
    if (timeElements.length > 0) {
        const updateTime = () => {
            const now = new Date();
            const timeString = now.toLocaleDateString() + " " + now.toLocaleTimeString();
            timeElements.forEach(el => el.textContent = timeString);
        };
        updateTime();
        setInterval(updateTime, 1000);
    }

    // -------------------------------------------------------------------------
    // 3. Real-Time Password Match Verification (Registration Form)
    // -------------------------------------------------------------------------
    const regPassword = document.getElementById("regPassword");
    const confirmPassword = document.getElementById("confirmPassword");
    const matchStatus = document.getElementById("passwordMatchStatus");
    const registerBtn = document.getElementById("registerSubmitBtn");

    if (regPassword && confirmPassword && matchStatus) {
        function validatePasswordMatch() {
            const p1 = regPassword.value;
            const p2 = confirmPassword.value;

            if (!p2) {
                matchStatus.textContent = "";
                matchStatus.className = "match-feedback";
                if (registerBtn) registerBtn.disabled = false;
                return;
            }

            if (p1 === p2) {
                matchStatus.textContent = "✓ Passwords match";
                matchStatus.className = "match-feedback match-success";
                if (registerBtn) registerBtn.disabled = false;
            } else {
                matchStatus.textContent = "✗ Passwords do not match";
                matchStatus.className = "match-feedback match-error";
                if (registerBtn) registerBtn.disabled = true;
            }
        }

        regPassword.addEventListener("input", validatePasswordMatch);
        confirmPassword.addEventListener("input", validatePasswordMatch);
    }

    // -------------------------------------------------------------------------
    // 4. Voting Booth Candidate Selection & Card Highlighting
    // -------------------------------------------------------------------------
    const candidateRadios = document.querySelectorAll(".candidate-radio");
    const openModalBtn = document.getElementById("openConfirmModalBtn");
    const modalCandidateName = document.getElementById("modalCandidateName");
    const modalCandidateSymbol = document.getElementById("modalCandidateSymbol");
    const confirmModal = document.getElementById("voteConfirmModal");
    const closeModalBtn = document.getElementById("closeConfirmModalBtn");
    const submitFinalVoteBtn = document.getElementById("submitFinalVoteBtn");
    const voteForm = document.getElementById("voteForm");

    if (candidateRadios.length > 0) {
        candidateRadios.forEach(radio => {
            radio.addEventListener("change", function () {
                document.querySelectorAll(".candidate-choice-card").forEach(card => {
                    card.classList.remove("selected");
                });

                const card = this.closest(".candidate-choice-card");
                if (card) {
                    card.classList.add("selected");
                }

                if (openModalBtn) {
                    openModalBtn.disabled = false;
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // 5. Ballot Confirmation Modal Management
    // -------------------------------------------------------------------------
    if (openModalBtn && confirmModal) {
        openModalBtn.addEventListener("click", function () {
            const selectedRadio = document.querySelector(".candidate-radio:checked");
            if (!selectedRadio) {
                alert("Please select a candidate before proceeding to cast your ballot.");
                return;
            }

            const candidateName = selectedRadio.getAttribute("data-candidate-name") || "Selected Candidate";
            const candidateSymbol = selectedRadio.getAttribute("data-candidate-symbol") || "🗳️";

            if (modalCandidateName) modalCandidateName.textContent = candidateName;
            if (modalCandidateSymbol) modalCandidateSymbol.textContent = candidateSymbol;

            confirmModal.style.display = "flex";
        });
    }

    if (closeModalBtn && confirmModal) {
        closeModalBtn.addEventListener("click", function () {
            confirmModal.style.display = "none";
        });
    }

    if (confirmModal) {
        confirmModal.addEventListener("click", function (e) {
            if (e.target === confirmModal) {
                confirmModal.style.display = "none";
            }
        });
    }

    // -------------------------------------------------------------------------
    // 6. Final Vote Submission & Double-Submit Protection
    // -------------------------------------------------------------------------
    if (submitFinalVoteBtn && voteForm) {
        submitFinalVoteBtn.addEventListener("click", function () {
            submitFinalVoteBtn.disabled = true;
            submitFinalVoteBtn.textContent = "Committing Ballot...";
            submitFinalVoteBtn.style.opacity = "0.7";
            voteForm.submit();
        });
    }

    // Prevent double form submissions on login and registration forms
    preventDoubleSubmit("loginForm", "loginSubmitBtn", "Authenticating...");
    preventDoubleSubmit("adminLoginForm", "adminLoginSubmitBtn", "Verifying Admin...");
    preventDoubleSubmit("registerForm", "registerSubmitBtn", "Creating Account...");

    // -------------------------------------------------------------------------
    // 7. Dynamic Progress Bar Widths (Electoral Results & Tabulation)
    // -------------------------------------------------------------------------
    document.querySelectorAll(".progress-bar-fill[data-progress]").forEach(function (bar) {
        var progress = parseFloat(bar.getAttribute("data-progress")) || 0;
        bar.style.width = Math.min(100, Math.max(0, progress)) + "%";
    });
});

/**
 * Attaches double-submit prevention to a form.
 * @param {string} formId 
 * @param {string} btnId 
 * @param {string} loadingText 
 */
function preventDoubleSubmit(formId, btnId, loadingText) {
    const form = document.getElementById(formId);
    const btn = document.getElementById(btnId);
    if (form && btn) {
        form.addEventListener("submit", function () {
            btn.disabled = true;
            btn.textContent = loadingText;
        });
    }
}

/**
 * Copies text content from an element to the system clipboard.
 * @param {string} elementId - ID of element containing text to copy
 * @param {HTMLButtonElement} btn - The clicked button element
 */
function copyToClipboard(elementId, btn) {
    const el = document.getElementById(elementId);
    if (!el) return;

    const text = el.innerText || el.textContent;
    navigator.clipboard.writeText(text.trim()).then(() => {
        const originalText = btn.innerHTML;
        btn.innerHTML = "✓ Copied!";
        btn.style.backgroundColor = "#059669";
        btn.style.borderColor = "#059669";
        btn.style.color = "#ffffff";
        setTimeout(() => {
            btn.innerHTML = originalText;
            btn.style.backgroundColor = "";
            btn.style.borderColor = "";
            btn.style.color = "";
        }, 2200);
    }).catch(err => {
        console.error("Clipboard copy failed: ", err);
        const textarea = document.createElement("textarea");
        textarea.value = text.trim();
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand("copy");
            btn.innerHTML = "✓ Copied!";
            setTimeout(() => { btn.innerHTML = originalText; }, 2200);
        } catch (e) {
            alert("Please copy the receipt manually.");
        }
        document.body.removeChild(textarea);
    });
}
