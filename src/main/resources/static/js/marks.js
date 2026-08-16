const isAdmin = () => getRole() === "ADMIN";
let lastLoadedIds = null; // { type: 'subject'|'exam', id }

if (guardPage(["ADMIN", "TEACHER"])) {
  renderLayout("marks", "Marks");
  init();
}

async function init() {
  await loadDropdowns();
  document.getElementById("load-btn").addEventListener("click", loadMarks);
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
    const exams = await api.get("/exams?size=200");
    ["lookup-exam", "f-exam"].forEach(id => {
      const select = document.getElementById(id);
      exams.content.forEach(e => select.insertAdjacentHTML("beforeend",
        `<option value="${e.id}">${escapeHtml(e.examName)} (${escapeHtml(e.examType)})</option>`));
    });
  } catch (err) { showError(err); }

  try {
    const students = await api.get("/students?size=200");
    const select = document.getElementById("f-student");
    students.content.forEach(s => select.insertAdjacentHTML("beforeend",
      `<option value="${s.id}">${escapeHtml(s.studentId)} - ${escapeHtml(s.name)}</option>`));
  } catch (err) { showError(err); }
}

async function loadMarks() {
  const subjectId = document.getElementById("lookup-subject").value;
  const examId = document.getElementById("lookup-exam").value;
  const body = document.getElementById("items-body");

  if (!subjectId && !examId) {
    showToast("Choose a subject or an exam first", "error");
    return;
  }

  body.innerHTML = `<tr><td colspan="8" class="loading-state">Loading...</td></tr>`;
  try {
    let list;
    if (subjectId) {
      list = await api.get(`/marks/subject/${subjectId}`);
      lastLoadedIds = { type: "subject", id: subjectId };
    } else {
      list = await api.get(`/marks/exam/${examId}`);
      lastLoadedIds = { type: "exam", id: examId };
    }
    renderItems(list);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="8" class="empty-state">Failed to load marks</td></tr>`;
    showError(err);
  }
}

function renderItems(list) {
  const body = document.getElementById("items-body");
  if (!list || list.length === 0) {
    body.innerHTML = `<tr><td colspan="8" class="empty-state">No marks recorded for this selection yet</td></tr>`;
    return;
  }
  body.innerHTML = list.map(m => `
    <tr>
      <td>${escapeHtml(m.studentName)}</td>
      <td>${escapeHtml(m.subjectName)}</td>
      <td>${escapeHtml(m.examName)}</td>
      <td>${m.marksObtained} / ${m.maximumMarks}</td>
      <td>${m.percentage.toFixed(1)}%</td>
      <td>${escapeHtml(m.grade)}</td>
      <td><span class="badge ${m.passed ? "badge-good" : "badge-fail"}">${m.passed ? "Pass" : "Fail"}</span></td>
      <td class="table-actions">
        <button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(m)})'>Edit</button>
        ${isAdmin() ? `<button class="btn btn-danger btn-sm" onclick="deleteItem(${m.id})">Delete</button>` : ""}
      </td>
    </tr>`).join("");
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Marks" : "Enter Marks";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-student").value = item ? item.studentId : "";
  document.getElementById("f-subject").value = item ? item.subjectId : "";
  document.getElementById("f-exam").value = item ? item.examId : "";
  document.getElementById("f-marksObtained").value = item ? item.marksObtained : "";
  document.getElementById("f-maximumMarks").value = item ? item.maximumMarks : 100;
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    studentId: Number(document.getElementById("f-student").value),
    subjectId: Number(document.getElementById("f-subject").value),
    examId: Number(document.getElementById("f-exam").value),
    marksObtained: Number(document.getElementById("f-marksObtained").value),
    maximumMarks: Number(document.getElementById("f-maximumMarks").value),
  };
  try {
    if (id) {
      await api.put(`/marks/${id}`, payload);
      showToast("Marks updated", "success");
    } else {
      await api.post("/marks", payload);
      showToast("Marks recorded", "success");
    }
    closeModal("item-modal");
    if (lastLoadedIds) loadMarks();
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this marks entry? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/marks/${id}`);
    showToast("Marks deleted", "success");
    if (lastLoadedIds) loadMarks();
  } catch (err) {
    showError(err);
  }
}
