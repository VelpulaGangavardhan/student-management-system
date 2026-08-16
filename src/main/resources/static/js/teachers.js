let currentPage = 0;
const isAdmin = () => getRole() === "ADMIN";

if (guardPage(["ADMIN", "TEACHER"])) {
  renderLayout("teachers", "Teachers");
  document.getElementById("add-btn").style.display = isAdmin() ? "inline-flex" : "none";
  init();
}

async function init() {
  await loadDepartments();
  await loadItems(0);
  document.getElementById("add-btn").addEventListener("click", () => openItemModal(null));
  document.getElementById("item-save-btn").addEventListener("click", saveItem);
  document.getElementById("filter-department").addEventListener("change", () => loadItems(0));
  let searchTimer;
  document.getElementById("search-input").addEventListener("input", () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadItems(0), 350);
  });
}

async function loadDepartments() {
  try {
    const page = await api.get("/departments?size=100");
    const filterSelect = document.getElementById("filter-department");
    const formSelect = document.getElementById("f-department");
    page.content.forEach(d => {
      filterSelect.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
      formSelect.insertAdjacentHTML("beforeend", `<option value="${d.id}">${escapeHtml(d.name)}</option>`);
    });
  } catch (err) { showError(err); }
}

async function loadItems(page) {
  currentPage = page;
  const body = document.getElementById("items-body");
  body.innerHTML = `<tr><td colspan="6" class="loading-state">Loading...</td></tr>`;
  const keyword = document.getElementById("search-input").value.trim();
  const departmentId = document.getElementById("filter-department").value;
  try {
    let result;
    if (keyword) {
      result = await api.get(`/teachers/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=10`);
    } else if (departmentId) {
      result = await api.get(`/teachers/filter?departmentId=${departmentId}&page=${page}&size=10`);
    } else {
      result = await api.get(`/teachers?page=${page}&size=10`);
    }
    renderItems(result);
  } catch (err) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">Failed to load teachers</td></tr>`;
    showError(err);
  }
}

function renderItems(page) {
  const body = document.getElementById("items-body");
  if (!page.content || page.content.length === 0) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">No teachers found</td></tr>`;
  } else {
    body.innerHTML = page.content.map(t => `
      <tr>
        <td>${escapeHtml(t.teacherId)}</td>
        <td>${escapeHtml(t.name)}</td>
        <td>${escapeHtml(t.email)}</td>
        <td>${escapeHtml(t.departmentName || "-")}</td>
        <td>${escapeHtml(t.specialization || "-")}</td>
        <td class="table-actions">
          ${isAdmin() ? `
            <button class="btn btn-secondary btn-sm" onclick='openItemModal(${JSON.stringify(t)})'>Edit</button>
            <button class="btn btn-danger btn-sm" onclick="deleteItem(${t.id})">Delete</button>
          ` : ""}
        </td>
      </tr>`).join("");
  }
  renderPagination(document.getElementById("pagination"), page, loadItems);
}

function openItemModal(item) {
  document.getElementById("item-form-error").textContent = "";
  document.getElementById("item-modal-title").textContent = item ? "Edit Teacher" : "Add Teacher";
  document.getElementById("item-id").value = item ? item.id : "";
  document.getElementById("f-teacherId").value = item ? item.teacherId : "";
  document.getElementById("f-name").value = item ? item.name : "";
  document.getElementById("f-email").value = item ? item.email : "";
  document.getElementById("f-phone").value = item ? item.phone : "";
  document.getElementById("f-qualification").value = item ? (item.qualification || "") : "";
  document.getElementById("f-specialization").value = item ? (item.specialization || "") : "";
  document.getElementById("f-department").value = item ? item.departmentId : "";
  document.getElementById("f-password").value = "";
  // Only offer to provision a login when creating a brand-new teacher -
  // editing an existing one shouldn't silently reset their credentials.
  document.getElementById("f-password-group").style.display = item ? "none" : "block";
  openModal("item-modal");
}

async function saveItem() {
  const errorEl = document.getElementById("item-form-error");
  errorEl.textContent = "";
  const id = document.getElementById("item-id").value;
  const payload = {
    teacherId: document.getElementById("f-teacherId").value.trim(),
    name: document.getElementById("f-name").value.trim(),
    email: document.getElementById("f-email").value.trim(),
    phone: document.getElementById("f-phone").value.trim(),
    qualification: document.getElementById("f-qualification").value.trim(),
    specialization: document.getElementById("f-specialization").value.trim(),
    departmentId: Number(document.getElementById("f-department").value),
  };
  if (!id) {
    const password = document.getElementById("f-password").value;
    if (password) payload.password = password;
  }
  try {
    if (id) {
      await api.put(`/teachers/${id}`, payload);
      showToast("Teacher updated", "success");
    } else {
      await api.post("/teachers", payload);
      showToast("Teacher added", "success");
    }
    closeModal("item-modal");
    loadItems(currentPage);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

async function deleteItem(id) {
  const ok = await confirmAction("Delete this teacher? This cannot be undone.");
  if (!ok) return;
  try {
    await api.del(`/teachers/${id}`);
    showToast("Teacher deleted", "success");
    loadItems(currentPage);
  } catch (err) {
    showError(err);
  }
}
