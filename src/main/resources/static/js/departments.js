let currentPage = 0;
const isAdmin = () => getRole() === "ADMIN";

if (guardPage(["ADMIN", "TEACHER", "STUDENT"])) {
  renderLayout("departments", "Departments");
  document.getElementById("add-btn").style.display = isAdmin() ? "inline-flex" : "none";
  loadItems(0);

  document.getElementById("add-btn").addEventListener("click", () => openItemModal(null));
  document.getElementById("item-save-btn").addEventListener("click", saveItem);

  let searchTimer;
  document.getElementById("search-input").addEventListener("input", () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadItems(0), 350);
  });
}

async function loadItems(page) {
  currentPage = page;
  const body = document.getElementById("items-body");
  body.innerHTML = `<tr><td colspan="7" class="loading-state">Loading...</td></tr>`;
  const keyword = document.getElementById("search-input").value.trim();
  try {
    const result = keyword
      ? await api.get(`/departments/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`)
      : await api.get(`/departments?page=${page}&size=10`);
    renderItems(result);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state">Failed to load departments</td></tr>`;
    showError(err);
  }
}

function renderItems(page) {
  const body = document.getElementById("items-body");
  if (!page.content || page.content.length === 0) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state">No departments found</td></tr>`;
  } else {
    body.innerHTML = page.content.map(d => `
      <tr>
        <td>${escapeHtml(d.code)}</td>
        <td>${escapeHtml(d.name)}</td>
        <td>${escapeHtml(d.description || "-")}</td>
        <td>${d.studentCount}</td>
        <td>${d.teacherCount}</td>
        <td>${d.subjectCount}</td>
        <td class="table-actions">
          ${isAdmin() ? `
            <button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(d)})'>Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteItem(${d.id})">Delete</button>
          ` : ""}
        </td>
      </tr>`).join("");
  }
  renderPagination(document.getElementById("pagination"), page, loadItems);
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Department" : "Add Department";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-name").value = item ? item.name : "";
  document.getElementById("f-code").value = item ? item.code : "";
  document.getElementById("f-description").value = item ? (item.description || "") : "";
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    name: document.getElementById("f-name").value.trim(),
    code: document.getElementById("f-code").value.trim(),
    description: document.getElementById("f-description").value.trim(),
  };
  try {
    if (id) {
      await api.put(`/departments/${id}`, payload);
      showToast("Department updated", "success");
    } else {
      await api.post("/departments", payload);
      showToast("Department added", "success");
    }
    closeModal("item-modal");
    loadItems(currentPage);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this department? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/departments/${id}`);
    showToast("Department deleted", "success");
    loadItems(currentPage);
  } catch (err) {
    showError(err);
  }
}
