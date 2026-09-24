/**
 * Online Voting System - Client Utility Scripts
 */

document.addEventListener("DOMContentLoaded", function () {
    // Live Client Timestamp Display
    const timeElement = document.getElementById("client-timestamp");
    if (timeElement) {
        const updateTime = () => {
            const now = new Date();
            timeElement.textContent = now.toLocaleDateString() + " " + now.toLocaleTimeString();
        };
        updateTime();
        setInterval(updateTime, 1000);
    }
});

/**
 * Copies snippet text to system clipboard.
 * @param {string} elementId - ID of element containing text
 * @param {HTMLButtonElement} btn - The clicked button
 */
function copyToClipboard(elementId, btn) {
    const el = document.getElementById(elementId);
    if (!el) return;

    const text = el.innerText || el.textContent;
    navigator.clipboard.writeText(text).then(() => {
        const originalText = btn.textContent;
        btn.textContent = "Copied!";
        btn.style.backgroundColor = "#10b981";
        setTimeout(() => {
            btn.textContent = originalText;
            btn.style.backgroundColor = "";
        }, 2000);
    }).catch(err => {
        console.error("Failed to copy text: ", err);
    });
}
