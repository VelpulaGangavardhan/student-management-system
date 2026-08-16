// Landing page behavior: smooth scroll, mobile menu, scroll-reveal
// animations, and a small, honest stats section.
//
// Only /api/departments is public (see SecurityConfig) - every other
// dashboard/count endpoint requires a JWT. So this page only fetches a real
// number for Departments, and uses static descriptive labels for the other
// three cards rather than faking numbers from an authenticated endpoint.

document.documentElement.style.scrollBehavior = "smooth";

// If someone is already logged in, offer a shortcut to their dashboard
// instead of forcing them off this page - the landing page itself stays
// fully viewable either way, per the "no auth required" requirement.
(function adjustNavForSession() {
  if (typeof getToken === "function" && getToken() && getRole()) {
    const dashboardUrl = dashboardUrlForRole(getRole());
    const loginLink = document.getElementById("nav-login-link");
    const registerLink = document.getElementById("nav-register-link");
    if (loginLink) {
      loginLink.textContent = "Go to Dashboard";
      loginLink.href = dashboardUrl;
    }
    if (registerLink) {
      registerLink.textContent = "Go to Dashboard";
      registerLink.href = dashboardUrl;
    }
  }
})();

// Mobile menu toggle
const menuToggle = document.getElementById("mobile-menu-toggle");
const mobileMenu = document.getElementById("mobile-menu");
if (menuToggle && mobileMenu) {
  menuToggle.addEventListener("click", () => mobileMenu.classList.toggle("open"));
  mobileMenu.querySelectorAll("a").forEach(link =>
    link.addEventListener("click", () => mobileMenu.classList.remove("open")));
}

// Scroll-reveal for cards/sections
if ("IntersectionObserver" in window) {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add("in-view");
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.15 });
  document.querySelectorAll(".reveal").forEach(el => observer.observe(el));
} else {
  document.querySelectorAll(".reveal").forEach(el => el.classList.add("in-view"));
}

// Stats: real department count (public endpoint), static labels elsewhere.
loadStats();

async function loadStats() {
  setStat("stat-students", "Every Student");
  setStat("stat-teachers", "Expert Faculty");
  setStat("stat-subjects", "Structured Curriculum");

  try {
    const page = await fetch("/api/departments?size=1").then(r => r.ok ? r.json() : null);
    if (page && typeof page.totalElements === "number") {
      setStat("stat-departments", String(page.totalElements));
    } else {
      setStat("stat-departments", "Multiple");
    }
  } catch (err) {
    setStat("stat-departments", "Multiple");
  }
}

function setStat(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}
