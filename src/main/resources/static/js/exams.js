let currentPage = 0;
const canEdit = () => getRole() === "ADMIN" || getRole() === "TEACHER";
const isAdmin = () => getRole() === "ADMIN";

if (guardPage(["ADMIN", "TEACHER", "STUDENT"])) {
  renderLayout("exams", "Exams");
  document.getElementById("add-btn").style.display = canEdit() ? "inline-flex" : "none";
  loadItems(0);
  document.getElementById("add-btn").addEventListener("click", () => openItemModal(null));
  document.getElementById("item-save-btn").addEventListener("click", saveItem);
  document.getElementById("filter-semester").addEventListener("change", () => loadItems(0));
}

async function loadItems(page) {
  currentPage = page;
  const body = document.getElementById("items-body");
  body.innerHTML = `<tr><td colspan="6" class="loading-state">Loading...</td></tr>`;
  const semester = document.getElementById("filter-semester").value;
  try {
    const result = semester
      ? await api.get(`/exams?semester=${semester}&page=${page}&size=10`)
      : await api.get(`/exams?page=${page}&size=10`);
    renderItems(result);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">Failed to load exams</td></tr>`;
    showError(err);
  }
}

function renderItems(page) {
  const body = document.getElementById("items-body");
  if (!page.content || page.content.length === 0) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">No exams found</td></tr>`;
  } else {
    body.innerHTML = page.content.map(e => `
      <tr>
        <td>${escapeHtml(e.examName)}</td>
        <td>${escapeHtml(e.examType)}</td>
        <td>${escapeHtml(e.date)}</td>
        <td>${e.semester ?? "-"}</td>
        <td>${escapeHtml(e.academicYear)}</td>
        <td class="table-actions">
          ${canEdit() ? `<button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(e)})'>Edit</button>` : ""}
          ${isAdmin() ? `<button class="btn btn-danger btn-sm" onclick="deleteItem(${e.id})">Delete</button>` : ""}
        </td>
      </tr>`).join("");
  }
  renderPagination(document.getElementById("pagination"), page, loadItems);
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Exam" : "Add Exam";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-examName").value = item ? item.examName : "";
  document.getElementById("f-examType").value = item ? item.examType : "INTERNAL";
  document.getElementById("f-date").value = item ? item.date : "";
  document.getElementById("f-semester").value = item ? (item.semester || "") : "";
  document.getElementById("f-academicYear").value = item ? item.academicYear : "";
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    examName: document.getElementById("f-examName").value.trim(),
    examType: document.getElementById("f-examType").value,
    date: document.getElementById("f-date").value,
    semester: Number(document.getElementById("f-semester").value) || null,
    academicYear: document.getElementById("f-academicYear").value.trim(),
  };
  try {
    if (id) {
      await api.put(`/exams/${id}`, payload);
      showToast("Exam updated", "success");
    } else {
      await api.post("/exams", payload);
      showToast("Exam added", "success");
    }
    closeModal("item-modal");
    loadItems(currentPage);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this exam? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/exams/${id}`);
    showToast("Exam deleted", "success");
    loadItems(currentPage);
  } catch (err) {
    showError(err);
  }
}
