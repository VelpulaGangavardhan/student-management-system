let currentPage = 0;
let departmentsCache = [];
const isAdmin = () => getRole() === "ADMIN";

if (guardPage(["ADMIN", "TEACHER"])) {
  renderLayout("students", "Students");
  document.getElementById("add-student-btn").style.display = isAdmin() ? "inline-flex" : "none";
  init();
}

async function init() {
  await loadDepartmentsIntoSelects();
  await loadStudents(0);

  document.getElementById("add-student-btn").addEventListener("click", () => openStudentModal(null));
  document.getElementById("student-save-btn").addEventListener("click", saveStudent);

  let searchTimer;
  document.getElementById("search-input").addEventListener("input", () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadStudents(0), 350);
  });
  document.getElementById("filter-department").addEventListener("change", () => loadStudents(0));
  document.getElementById("filter-status").addEventListener("change", () => loadStudents(0));
}

async function loadDepartmentsIntoSelects() {
  try {
    const page = await api.get("/departments?size=100");
    departmentsCache = page.content;
    const filterSelect = document.getElementById("filter-department");
    const formSelect = document.getElementById("f-department");
    departmentsCache.forEach(d => {
      filterSelect.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
      formSelect.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
    });
  } catch (err) {
    showError(err);
  }
}

async function loadStudents(page) {
  currentPage = page;
  const body = document.getElementById("students-body");
  body.innerHTML = `<tr><td colspan="8" class="loading-state">Loading...</td></tr>`;

  const keyword = document.getElementById("search-input").value.trim();
  const departmentId = document.getElementById("filter-department").value;
  const status = document.getElementById("filter-status").value;

  try {
    let result;
    if (keyword) {
      result = await api.get(`/students/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
    } else if (departmentId) {
      result = await api.get(`/students/filter?departmentId=${departmentId}&page=${page}&size=10`);
    } else if (status) {
      result = await api.get(`/students/filter?status=${status}&page=${page}&size=10`);
    } else {
      result = await api.get(`/students?page=${page}&size=10`);
    }
    renderStudents(result);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="8" class="empty-state">Failed to load students</td></tr>`;
    showError(err);
  }
}

function renderStudents(page) {
  const body = document.getElementById("students-body");
  if (!page.content || page.content.length === 0) {
    body.innerHTML = `<tr><td colspan="8" class="empty-state">No students found</td></tr>`;
  } else {
    body.innerHTML = page.content.map(s => `
      <tr>
        <td>${escapeHtml(s.studentId)}</td>
        <td>${escapeHtml(s.name)}</td>
        <td>${escapeHtml(s.email)}</td>
        <td>${escapeHtml(s.departmentName || "-")}</td>
        <td>${s.year ?? "-"} / ${s.semester ?? "-"}</td>
        <td>${s.cgpa ?? "-"}</td>
        <td><span class="badge badge-${s.status === "ACTIVE" ? "active" : "inactive"}">${escapeHtml(s.status || "-")}</span></td>
        <td class="table-actions">
          ${isAdmin() ? `
            <button class="btn btn-secondary btn-sm" onclick='openStudentModal(${JSON.stringify(s)})'>Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteStudent(${s.id})">Delete</button>
          ` : ""}
        </td>
      </tr>`).join("");
  }
  renderPagination(document.getElementById("pagination"), page, loadStudents);
}

function openStudentModal(student) {
  document.getElementById("student-form-error").textContent = "";
  document.getElementById("student-modal-title").textContent = student ? "Edit Student" : "Add Student";
  document.getElementById("student-id").value = student ? student.id : "";
  document.getElementById("f-studentId").value = student ? student.studentId : "";
  document.getElementById("f-name").value = student ? student.name : "";
  document.getElementById("f-email").value = student ? student.email : "";
  document.getElementById("f-phone").value = student ? student.phone : "";
  document.getElementById("f-dob").value = student ? (student.dateOfBirth || "") : "";
  document.getElementById("f-gender").value = student ? (student.gender || "") : "";
  document.getElementById("f-address").value = student ? (student.address || "") : "";
  document.getElementById("f-department").value = student ? student.departmentId : "";
  document.getElementById("f-year").value = student ? (student.year || "") : "";
  document.getElementById("f-semester").value = student ? (student.semester || "") : "";
  document.getElementById("f-cgpa").value = student ? (student.cgpa ?? "") : "";
  document.getElementById("f-admission").value = student ? (student.admissionDate || "") : "";
  document.getElementById("f-status").value = student ? student.status : "ACTIVE";
  openModal("student-modal");
}

async function saveStudent() {
  const errorEl = document.getElementById("student-form-error");
  errorEl.textContent = "";

  const id = document.getElementById("student-id").value;
  const payload = {
    studentId: document.getElementById("f-studentId").value.trim(),
    name: document.getElementById("f-name").value.trim(),
    email: document.getElementById("f-email").value.trim(),
    phone: document.getElementById("f-phone").value.trim(),
    dateOfBirth: document.getElementById("f-dob").value || null,
    gender: document.getElementById("f-gender").value || null,
    address: document.getElementById("f-address").value.trim(),
    departmentId: Number(document.getElementById("f-department").value),
    year: Number(document.getElementById("f-year").value) || null,
    semester: Number(document.getElementById("f-semester").value) || null,
    cgpa: document.getElementById("f-cgpa").value ? Number(document.getElementById("f-cgpa").value) : null,
    admissionDate: document.getElementById("f-admission").value,
    status: document.getElementById("f-status").value,
  };

  try {
    if (id) {
      await api.put(`/students/${id}`, payload);
      showToast("Student updated", "success");
    } else {
      await api.post("/students", payload);
      showToast("Student added", "success");
    }
    closeModal("student-modal");
    loadStudents(currentPage);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteStudent(id) {
  const ok = await confirmAction("Delete this student? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/students/${id}`);
    showToast("Student deleted", "success");
    loadStudents(currentPage);
  } catch (err) {
    showError(err);
  }
}
