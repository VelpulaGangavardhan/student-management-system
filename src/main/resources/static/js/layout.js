/**
 * Renders the shared sidebar + navbar. Which links appear depends on the
 * logged-in role, so this single function drives navigation for all three
 * roles instead of duplicating markup on every page.
 */
const NAV_LINKS = {
  ADMIN: [
    { href: "admin-dashboard.html", label: "Dashboard", key: "dashboard" },
    { href: "students.html", label: "Students", key: "students" },
    { href: "teachers.html", label: "Teachers", key: "teachers" },
    { href: "departments.html", label: "Departments", key: "departments" },
    { href: "subjects.html", label: "Subjects", key: "subjects" },
    { href: "exams.html", label: "Exams", key: "exams" },
    { href: "marks.html", label: "Marks", key: "marks" },
    { href: "attendance.html", label: "Attendance", key: "attendance" },
    { href: "results.html", label: "Results", key: "results" },
  ],
  TEACHER: [
    { href: "teacher-dashboard.html", label: "Dashboard", key: "dashboard" },
    { href: "students.html", label: "Students", key: "students" },
    { href: "subjects.html", label: "Subjects", key: "subjects" },
    { href: "exams.html", label: "Exams", key: "exams" },
    { href: "marks.html", label: "Marks", key: "marks" },
    { href: "attendance.html", label: "Attendance", key: "attendance" },
    { href: "results.html", label: "Results", key: "results" },
  ],
  STUDENT: [
    { href: "student-dashboard.html", label: "Dashboard", key: "dashboard" },
    { href: "results.html", label: "Results", key: "results" },
  ],
};

function renderLayout(activeKey, pageTitle) {
  const role = getRole();
  const username = getUsername();
  const links = NAV_LINKS[role] || [];

  const sidebar = document.getElementById("sidebar");
  if (sidebar) {
    sidebar.innerHTML = `
      <div class="sidebar-brand">Smart<span>SMS</span></div>
      <nav class="sidebar-nav">
        ${links.map(l => `<a href="${l.href}" class="${l.key === activeKey ? "active" : ""}">${l.label}</a>`).join("")}
      </nav>
      <div class="sidebar-footer">Smart Student Management System</div>
    `;
  }

  const navbar = document.getElementById("navbar");
  if (navbar) {
    navbar.innerHTML = `
      <h1>${pageTitle}</h1>
      <div class="user-info">
        <span>${escapeHtml(username || "")}</span>
        <span class="badge-role">${escapeHtml(role || "")}</span>
        <button class="btn-link-logout" id="logout-btn">Log out</button>
      </div>
    `;
    document.getElementById("logout-btn").addEventListener("click", logout);
  }
}
