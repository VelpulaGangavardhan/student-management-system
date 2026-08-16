if (guardPage(["ADMIN", "TEACHER", "STUDENT"])) {
  renderLayout("results", "Results");
  init();
}

async function init() {
  if (getRole() === "STUDENT") {
    document.getElementById("lookup-panel").style.display = "none";
    try {
      const me = await api.get("/students/me");
      loadResult(me.id);
    } catch (err) {
      showError(err);
    }
  } else {
    await loadStudentDropdown();
    document.getElementById("load-btn").addEventListener("click", () => {
      const id = document.getElementById("lookup-student").value;
      if (!id) {
        showToast("Select a student first", "error");
        return;
      }
      loadResult(id);
    });
  }
}

async function loadStudentDropdown() {
  try {
    const students = await api.get("/students?size=200");
    const select = document.getElementById("lookup-student");
    students.content.forEach(s => select.insertAdjacentHTML("beforeend",
      `<option value="${s.id}">${escapeHtml(s.studentId)} - ${escapeHtml(s.name)}</option>`));
  } catch (err) {
    showError(err);
  }
}

async function loadResult(studentId) {
  try {
    const [result, performance] = await Promise.all([
      api.get(`/results/student/${studentId}`),
      api.get(`/results/student/${studentId}/performance`),
    ]);
    renderResult(result, performance);
  } catch (err) {
    showError(err);
  }
}

function renderResult(result, performance) {
  const grid = document.getElementById("stats-grid");
  grid.style.display = "grid";
  const statusClass = performance.status === "GOOD" ? "good" : performance.status === "AT_RISK" ? "risk" : "needs";
  grid.innerHTML = `
    <div class="stat-card"><div class="stat-label">Total Marks</div><div class="stat-value">${result.totalMarksObtained} / ${result.totalMaximumMarks}</div></div>
    <div class="stat-card"><div class="stat-label">Overall %</div><div class="stat-value">${result.overallPercentage.toFixed(1)}%</div></div>
    <div class="stat-card"><div class="stat-label">Overall Grade</div><div class="stat-value">${escapeHtml(result.overallGrade)}</div></div>
    <div class="stat-card"><div class="stat-label">GPA (approx.)</div><div class="stat-value">${result.gpa}</div></div>
    <div class="stat-card ${result.overallPass ? "accent-success" : "accent-danger"}">
      <div class="stat-label">Overall Status</div>
      <div class="stat-value">${result.overallPass ? "PASS" : "FAIL"}</div>
    </div>
  `;

  const subjectsPanel = document.getElementById("subjects-panel");
  subjectsPanel.style.display = "block";
  const body = document.getElementById("marks-body");
  if (!result.subjectResults || result.subjectResults.length === 0) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state">No marks recorded yet</td></tr>`;
  } else {
    body.innerHTML = result.subjectResults.map(m => `
      <tr>
        <td>${escapeHtml(m.subjectName)}</td>
        <td>${escapeHtml(m.examName)}</td>
        <td>${m.marksObtained} / ${m.maximumMarks}</td>
        <td>${m.percentage.toFixed(1)}%</td>
        <td>${escapeHtml(m.grade)}</td>
        <td><span class="badge ${m.passed ? "badge-good" : "badge-fail"}">${m.passed ? "Pass" : "Fail"}</span></td>
      </tr>`).join("");
  }

  const perfPanel = document.getElementById("performance-panel");
  perfPanel.style.display = "block";
  document.getElementById("performance-body").innerHTML = `
    <p><span class="badge badge-${statusClass}" style="font-size:14px;">${performance.status.replace("_", " ")}</span>
       &nbsp; Performance score: <strong>${performance.performanceScore.toFixed(1)} / 100</strong></p>
    <ul>${performance.reasons.map(r => `<li>${escapeHtml(r)}</li>`).join("")}</ul>
    <p style="color:var(--color-text-muted); font-size:13px;">
      This is a rule-based assessment (CGPA, attendance %, and failed-subject count against fixed thresholds) - not a machine-learning prediction.
    </p>
  `;
}
