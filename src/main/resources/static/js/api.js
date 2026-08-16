/**
 * Thin fetch wrapper shared by every page. Centralizes:
 *  - attaching the JWT to every request
 *  - redirecting to login on 401
 *  - turning the backend's ErrorResponse JSON into a readable message
 *  - toast notifications and a reusable confirm dialog
 */
const API_BASE = "/api";

function getToken() { return localStorage.getItem("sms_token"); }
function getRole() { return localStorage.getItem("sms_role"); }
function getUsername() { return localStorage.getItem("sms_username"); }

function setSession(token, username, role) {
  localStorage.setItem("sms_token", token);
  localStorage.setItem("sms_username", username);
  localStorage.setItem("sms_role", role);
}

function clearSession() {
  localStorage.removeItem("sms_token");
  localStorage.removeItem("sms_username");
  localStorage.removeItem("sms_role");
}

function logout() {
  clearSession();
  // Requirement: logout must land on the public home page, never the login
  // page. location.replace (not .href) also drops the authenticated page
  // from forward-navigation history, so "forward" after logout can't
  // resurface it either.
  window.location.replace("/");
}

/**
 * Redirects to login.html if there's no token, or to the correct dashboard
 * if the logged-in role doesn't match the page's expected role(s).
 * Call at the top of every protected page.
 *
 * Also guards against the browser back/forward-cache (bfcache): if the user
 * logs out and then presses Back, some browsers restore the previous page
 * from an in-memory snapshot WITHOUT re-running its <script> tags - so the
 * original guardPage() call that ran at page load never fires again, and
 * the stale dashboard could flash on screen. The `pageshow` listener below
 * re-runs this same check every time the page becomes visible again
 * (event.persisted is true specifically for bfcache restores), so a logged
 * -out session is caught and redirected immediately either way.
 */
function guardPage(allowedRoles) {
  const check = () => {
    const token = getToken();
    const role = getRole();
    if (!token || !role) {
      window.location.replace("login.html");
      return false;
    }
    if (allowedRoles && !allowedRoles.includes(role)) {
      window.location.replace(dashboardUrlForRole(role));
      return false;
    }
    return true;
  };

  window.addEventListener("pageshow", (event) => {
    if (event.persisted) check();
  });

  return check();
}

function dashboardUrlForRole(role) {
  if (role === "ADMIN") return "admin-dashboard.html";
  if (role === "TEACHER") return "teacher-dashboard.html";
  return "student-dashboard.html";
}

async function apiFetch(path, options = {}) {
  const headers = Object.assign({ "Content-Type": "application/json" }, options.headers || {});
  const token = getToken();
  if (token) headers["Authorization"] = "Bearer " + token;

  const response = await fetch(API_BASE + path, Object.assign({}, options, { headers }));

  if (response.status === 401) {
    clearSession();
    window.location.href = "login.html";
    throw new Error("Session expired. Please log in again.");
  }

  if (response.status === 204) {
    return null;
  }

  const isJson = (response.headers.get("content-type") || "").includes("application/json");
  const body = isJson ? await response.json() : null;

  if (!response.ok) {
    const message = body && body.message ? body.message : "Request failed (" + response.status + ")";
    const error = new Error(message);
    error.validationErrors = body ? body.validationErrors : null;
    error.status = response.status;
    throw error;
  }

  return body;
}

const api = {
  get: (path) => apiFetch(path, { method: "GET" }),
  post: (path, data) => apiFetch(path, { method: "POST", body: JSON.stringify(data) }),
  put: (path, data) => apiFetch(path, { method: "PUT", body: JSON.stringify(data) }),
  patch: (path) => apiFetch(path, { method: "PATCH" }),
  del: (path) => apiFetch(path, { method: "DELETE" }),
};

/* ---------------- Toasts ---------------- */
function ensureToastContainer() {
  let el = document.getElementById("toast-container");
  if (!el) {
    el = document.createElement("div");
    el.id = "toast-container";
    document.body.appendChild(el);
  }
  return el;
}

function showToast(message, type = "info") {
  const container = ensureToastContainer();
  const toast = document.createElement("div");
  toast.className = "toast " + type;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}

function showError(err) {
  console.error(err);
  if (err && err.validationErrors) {
    const first = Object.values(err.validationErrors)[0];
    showToast(first || err.message, "error");
    return;
  }
  showToast((err && err.message) || "Something went wrong", "error");
}

/* ---------------- Confirm dialog ---------------- */
function confirmAction(message) {
  return new Promise((resolve) => {
    let overlay = document.getElementById("confirm-overlay");
    if (!overlay) {
      overlay = document.createElement("div");
      overlay.id = "confirm-overlay";
      overlay.className = "modal-overlay";
      overlay.innerHTML = `
        <div class="modal" style="max-width:380px;">
          <div class="modal-header"><h3>Please confirm</h3></div>
          <div class="modal-body"><p class="confirm-text" id="confirm-text"></p></div>
          <div class="modal-footer">
            <button class="btn btn-secondary" id="confirm-cancel">Cancel</button>
            <button class="btn btn-danger" id="confirm-ok">Confirm</button>
          </div>
        </div>`;
      document.body.appendChild(overlay);
    }
    document.getElementById("confirm-text").textContent = message;
    overlay.classList.add("open");

    const cleanup = (result) => {
      overlay.classList.remove("open");
      okBtn.removeEventListener("click", onOk);
      cancelBtn.removeEventListener("click", onCancel);
      resolve(result);
    };
    const okBtn = document.getElementById("confirm-ok");
    const cancelBtn = document.getElementById("confirm-cancel");
    const onOk = () => cleanup(true);
    const onCancel = () => cleanup(false);
    okBtn.addEventListener("click", onOk);
    cancelBtn.addEventListener("click", onCancel);
  });
}

/* ---------------- Small render helpers ---------------- */
function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;").replace(/'/g, "&#039;");
}

function renderPagination(containerEl, page, onPageChange) {
  // page: Spring Data Page object { number, totalPages, totalElements, first, last }
  containerEl.innerHTML = "";
  if (!page || page.totalPages <= 1) return;
  const info = document.createElement("span");
  info.textContent = `Page ${page.number + 1} of ${page.totalPages} (${page.totalElements} total)`;
  const prev = document.createElement("button");
  prev.className = "btn btn-secondary btn-sm";
  prev.textContent = "Prev";
  prev.disabled = page.first;
  prev.onclick = () => onPageChange(page.number - 1);
  const next = document.createElement("button");
  next.className = "btn btn-secondary btn-sm";
  next.textContent = "Next";
  next.disabled = page.last;
  next.onclick = () => onPageChange(page.number + 1);
  containerEl.appendChild(info);
  containerEl.appendChild(prev);
  containerEl.appendChild(next);
}

function openModal(id) { document.getElementById(id).classList.add("open"); }
function closeModal(id) { document.getElementById(id).classList.remove("open"); }
