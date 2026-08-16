const isAdmin = () => getRole() === "ADMIN";
let lastLoaded = null;

if (guardPage(["ADMIN", "TEACHER"])) {
  renderLayout("attendance", "Attendance");
  init();
}

async function init() {
  await loadDropdowns();
  document.getElementById("load-btn").addEventListener("click", loadAttendance);
  document.getElementById("add-btn").addEventListener("click", () => openItemModal());
  document.getElementById("item-save-btn").addEventListener("click", saveItem);
}

async function loadDropdowns() {
  try {
    const subjects = await api.get("/subjects?size=200");
    ["lookup-subject", "f-subject"].forEach(id => {
      const select = document.getElementById(id);
      subjects.content.forEach(s => select.insertAdjacentHTML("beforeend",
        `<option value="${s.id}">${escapeHtml(s.code)} - ${escapeHtml(s.name)}</option>`));
    });
  } catch (err) { showError(err); }

  try {
    const students = await api.get("/students?size=200");
    ["lookup-student", "f-student"].forEach(id => {
      const select = document.getElementById(id);
      students.content.forEach(s => select.insertAdjacentHTML("beforeend",
        `<option value="${s.id}">${escapeHtml(s.studentId)} - ${escapeHtml(s.name)}</option>`));
    });
  } catch (err) { showError(err); }
}

async function loadAttendance() {
  const subjectId = document.getElementById("lookup-subject").value;
  const studentId = document.getElementById("lookup-student").value;
  const body = document.getElementById("items-body");
  const summaryBar = document.getElementById("summary-bar");
  summaryBar.style.display = "none";

  if (!subjectId && !studentId) {
    showToast("Choose a subject or a student first", "error");
    return;
  }

  body.innerHTML = `<tr><td colspan="5" class="loading-state">Loading...</td></tr>`;
  try {
    let list;
    if (studentId) {
      list = await api.get(`/attendance/student/${studentId}`);
      lastLoaded = { type: "student", id: studentId };
      const summary = await api.get(`/attendance/student/${studentId}/summary`);
      summaryBar.style.display = "block";
      summaryBar.innerHTML = `
        <span class="badge ${summary.atRisk ? "badge-risk" : "badge-good"}">
          ${summary.presentClasses}/${summary.totalClasses} classes present
          &mdash; ${summary.attendancePercentage.toFixed(1)}%
          ${summary.atRisk ? " (Attendance Warning: below 75%)" : ""}
        </span>`;
    } else {
      list = await api.get(`/attendance/subject/${subjectId}`);
      lastLoaded = { type: "subject", id: subjectId };
    }
    renderItems(list);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="5" class="empty-state">Failed to load attendance</td></tr>`;
    showError(err);
  }
}

function renderItems(list) {
  const body = document.getElementById("items-body");
  if (!list || list.length === 0) {
    body.innerHTML = `<tr><td colspan="5" class="empty-state">No attendance records for this selection yet</td></tr>`;
    return;
  }
  body.innerHTML = list.map(a => `
    <tr>
      <td>${escapeHtml(a.studentName)}</td>
      <td>${escapeHtml(a.subjectName)}</td>
      <td>${escapeHtml(a.date)}</td>
      <td><span class="badge badge-${a.status === "PRESENT" ? "present" : "absent"}">${escapeHtml(a.status)}</span></td>
      <td class="table-actions">
        <button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(a)})'>Edit</button>
        ${isAdmin() ? `<button class="btn btn-danger btn-sm" onclick="deleteItem(${a.id})">Delete</button>` : ""}
      </td>
    </tr>`).join("");
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Attendance" : "Record Attendance";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-student").value = item ? item.studentId : "";
  document.getElementById("f-subject").value = item ? item.subjectId : "";
  document.getElementById("f-date").value = item ? item.date : new Date().toISOString().slice(0, 10);
  document.getElementById("f-status").value = item ? item.status : "PRESENT";
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    studentId: Number(document.getElementById("f-student").value),
    subjectId: Number(document.getElementById("f-subject").value),
    date: document.getElementById("f-date").value,
    status: document.getElementById("f-status").value,
  };
  try {
    if (id) {
      await api.put(`/attendance/${id}`, payload);
      showToast("Attendance updated", "success");
    } else {
      await api.post("/attendance", payload);
      showToast("Attendance recorded", "success");
    }
    closeModal("item-modal");
    if (lastLoaded) loadAttendance();
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this attendance record? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/attendance/${id}`);
    showToast("Attendance deleted", "success");
    if (lastLoaded) loadAttendance();
  } catch (err) {
    showError(err);
  }
}
