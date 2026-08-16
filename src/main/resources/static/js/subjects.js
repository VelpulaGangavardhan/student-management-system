let currentPage = 0;
const isAdmin = () => getRole() === "ADMIN";

if (guardPage(["ADMIN", "TEACHER", "STUDENT"])) {
  renderLayout("subjects", "Subjects");
  document.getElementById("add-btn").style.display = isAdmin() ? "inline-flex" : "none";
  init();
}

async function init() {
  await loadDropdowns();
  await loadItems(0);
  document.getElementById("add-btn").addEventListener("click", () => openItemModal(null));
  document.getElementById("item-save-btn").addEventListener("click", saveItem);
  document.getElementById("filter-department").addEventListener("change", () => loadItems(0));
  document.getElementById("filter-semester").addEventListener("change", () => loadItems(0));
  let searchTimer;
  document.getElementById("search-input").addEventListener("input", () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadItems(0), 350);
  });
}

async function loadDropdowns() {
  try {
    const depts = await api.get("/departments?size=100");
    const deptFilter = document.getElementById("filter-department");
    const deptForm = document.getElementById("f-department");
    depts.content.forEach(d => {
      deptFilter.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
      deptForm.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
    });
  } catch (err) { showError(err); }

  if (isAdmin()) {
    try {
      const teachers = await api.get("/teachers?size=200");
      const teacherForm = document.getElementById("f-teacher");
      teachers.content.forEach(t => {
        teacherForm.insertAdjacentHTML("beforeend", `<option value="${t.id}">${escapeHtml(t.name)}</option>`);
      });
    } catch (err) { /* non-fatal for non-admin views */ }
  }
}

async function loadItems(page) {
  currentPage = page;
  const body = document.getElementById("items-body");
  body.innerHTML = `<tr><td colspan="7" class="loading-state">Loading...</td></tr>`;
  const keyword = document.getElementById("search-input").value.trim();
  const departmentId = document.getElementById("filter-department").value;
  const semester = document.getElementById("filter-semester").value;
  try {
    let result;
    if (keyword) {
      result = await api.get(`/subjects/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
    } else if (departmentId || semester) {
      const params = new URLSearchParams({ page, size: 10 });
      if (departmentId) params.set("departmentId", departmentId);
      if (semester) params.set("semester", semester);
      result = await api.get(`/subjects/filter?${params.toString()}`);
    } else {
      result = await api.get(`/subjects?page=${page}&size=10`);
    }
    renderItems(result);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state">Failed to load subjects</td></tr>`;
    showError(err);
  }
}

function renderItems(page) {
  const body = document.getElementById("items-body");
  if (!page.content || page.content.length === 0) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state">No subjects found</td></tr>`;
  } else {
    body.innerHTML = page.content.map(s => `
      <tr>
        <td>${escapeHtml(s.code)}</td>
        <td>${escapeHtml(s.name)}</td>
        <td>${s.credits ?? "-"}</td>
        <td>${s.semester ?? "-"}</td>
        <td>${escapeHtml(s.departmentName || "-")}</td>
        <td>${escapeHtml(s.teacherName || "Unassigned")}</td>
        <td class="table-actions">
          ${isAdmin() ? `
            <button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(s)})'>Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteItem(${s.id})">Delete</button>
          ` : ""}
        </td>
      </tr>`).join("");
  }
  renderPagination(document.getElementById("pagination"), page, loadItems);
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Subject" : "Add Subject";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-code").value = item ? item.code : "";
  document.getElementById("f-name").value = item ? item.name : "";
  document.getElementById("f-credits").value = item ? (item.credits || "") : "";
  document.getElementById("f-semester").value = item ? (item.semester || "") : "";
  document.getElementById("f-department").value = item ? item.departmentId : "";
  document.getElementById("f-teacher").value = item ? (item.teacherId || "") : "";
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    code: document.getElementById("f-code").value.trim(),
    name: document.getElementById("f-name").value.trim(),
    credits: Number(document.getElementById("f-credits").value) || null,
    semester: Number(document.getElementById("f-semester").value) || null,
    departmentId: Number(document.getElementById("f-department").value),
    teacherId: document.getElementById("f-teacher").value ? Number(document.getElementById("f-teacher").value) : null,
  };
  try {
    if (id) {
      await api.put(`/subjects/${id}`, payload);
      showToast("Subject updated", "success");
    } else {
      await api.post("/subjects", payload);
      showToast("Subject added", "success");
    }
    closeModal("item-modal");
    loadItems(currentPage);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this subject? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/subjects/${id}`);
    showToast("Subject deleted", "success");
    loadItems(currentPage);
  } catch (err) {
    showError(err);
  }
}
