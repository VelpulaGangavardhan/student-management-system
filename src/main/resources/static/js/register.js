// Already logged in? No need to register again.
if (getToken() && getRole()) {
  window.location.href = dashboardUrlForRole(getRole());
}

loadDepartments();

document.getElementById("register-form").addEventListener("submit", handleSubmit);

async function loadDepartments() {
  const select = document.getElementById("r-department");
  try {
    const page = await api.get("/departments?size=100");
    if (!page.content || page.content.length === 0) {
      select.innerHTML = `<option value="">No departments available - contact admin</option>`;
      return;
    }
    select.innerHTML = `<option value="">Select Department</option>` +
      page.content.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join("");
  } catch (err) {
    select.innerHTML = `<option value="">Could not load departments</option>`;
    showError(err);
  }
}

function validate() {
  const name = document.getElementById("r-name").value.trim();
  const email = document.getElementById("r-email").value.trim();
  const phone = document.getElementById("r-phone").value.trim();
  const password = document.getElementById("r-password").value;
  const confirmPassword = document.getElementById("r-confirm-password").value;
  const studentId = document.getElementById("r-student-id").value.trim();
  const departmentId = document.getElementById("r-department").value;
  const year = document.getElementById("r-year").value;
  const semester = document.getElementById("r-semester").value;
  const dob = document.getElementById("r-dob").value;

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phonePattern = /^[0-9]{10}$/;

  if (!name) return "Full name is required.";
  if (!email || !emailPattern.test(email)) return "Please enter a valid email address.";
  if (!phone || !phonePattern.test(phone)) return "Please enter a valid 10-digit phone number.";
  if (!password || password.length < 6) return "Password must be at least 6 characters.";
  if (password !== confirmPassword) return "Passwords do not match.";
  if (!studentId) return "Student ID is required.";
  if (!departmentId) return "Please select a department.";
  if (!year) return "Year is required.";
  if (!semester) return "Semester is required.";
  if (!dob) return "Date of birth is required.";

  return null;
}

async function handleSubmit(e) {
  e.preventDefault();
  const errorEl = document.getElementById("register-error");
  const btn = document.getElementById("register-btn");
  errorEl.textContent = "";

  const validationMessage = validate();
  if (validationMessage) {
    errorEl.textContent = validationMessage;
    return;
  }

  const payload = {
    name: document.getElementById("r-name").value.trim(),
    email: document.getElementById("r-email").value.trim(),
    phone: document.getElementById("r-phone").value.trim(),
    password: document.getElementById("r-password").value,
    studentId: document.getElementById("r-student-id").value.trim(),
    departmentId: Number(document.getElementById("r-department").value),
    year: Number(document.getElementById("r-year").value),
    semester: Number(document.getElementById("r-semester").value),
    dateOfBirth: document.getElementById("r-dob").value,
    gender: document.getElementById("r-gender").value || null,
    address: document.getElementById("r-address").value.trim(),
  };

  btn.disabled = true;
  btn.textContent = "Creating account...";

  try {
    await api.post("/auth/register", payload);
    window.location.href = "login.html?registered=1";
  } catch (err) {
    errorEl.textContent = err.message || "Registration failed";
    btn.disabled = false;
    btn.textContent = "Create Account";
  }
}
