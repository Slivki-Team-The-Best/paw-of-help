const TOKEN_KEY = "pawHelpToken";
const ROLE_KEY = "pawHelpRole";

let cachedMe = null;

const TASK_TYPES = [
    "MEDICAL",
    "TRANSPORT",
    "FOSTER",
    "CARE",
    "EVENT",
    "REPAIR",
    "PHOTO",
    "FUNDRAISING",
    "OTHER",
];
const PRIORITIES = ["URGENT", "HIGH", "NORMAL", "LOW"];

function token() {
    return localStorage.getItem(TOKEN_KEY);
}

function role() {
    return localStorage.getItem(ROLE_KEY);
}

function setSession(t, r) {
    cachedMe = null;
    if (t) localStorage.setItem(TOKEN_KEY, t);
    else localStorage.removeItem(TOKEN_KEY);
    if (r) localStorage.setItem(ROLE_KEY, r);
    else localStorage.removeItem(ROLE_KEY);
    updateNav();
}

function authHeaders() {
    const h = {"Content-Type": "application/json"};
    const t = token();
    if (t) h["Authorization"] = "Bearer " + t;
    return h;
}

async function api(path, opts = {}) {
    const res = await fetch(path, {
        ...opts,
        headers: {...authHeaders(), ...(opts.headers || {})},
    });
    const text = await res.text();
    let body = null;
    if (text) {
        try {
            body = JSON.parse(text);
        } catch {
            body = text;
        }
    }
    if (!res.ok) {
        const msg =
            typeof body === "object" && body && body.error
                ? body.error
                : res.statusText;
        throw new Error(msg || "Ошибка запроса");
    }
    return body;
}

function showFlash(message, ok) {
    const el = document.getElementById("flash");
    el.textContent = message;
    el.className = "flash " + (ok ? "ok" : "err");
    el.hidden = false;
    setTimeout(() => {
        el.hidden = true;
    }, 6000);
}

function showView(name) {
    document.querySelectorAll(".view").forEach((v) => {
        v.hidden = v.id !== "view-" + name;
    });
}

function updateNav() {
    const loggedIn = !!token();
    document.getElementById("logoutBtn").hidden = !loggedIn;
    const canCreate =
        loggedIn &&
        ["PET_OWNER", "SHELTER_STAFF", "ADMIN"].includes(role() || "");
    document.getElementById("task-create-wrap").hidden = !canCreate;
}

document.getElementById("logoutBtn").addEventListener("click", () => {
    setSession(null, null);
    showFlash("Вы вышли", true);
    showView("home");
});

document.querySelectorAll("[data-view]").forEach((btn) => {
    btn.addEventListener("click", () => {
        const name = btn.getAttribute("data-view");
        if (name === "tasks") loadTasks();
        if (name === "profile") loadProfile();
        showView(name);
    });
});

document.getElementById("form-register").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    try {
        const data = await api("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({
                email: fd.get("email"),
                password: fd.get("password"),
                fullName: fd.get("fullName"),
                role: fd.get("role"),
            }),
        });
        setSession(data.token, data.role);
        showFlash("Регистрация успешна", true);
        showView("tasks");
        loadTasks();
    } catch (err) {
        showFlash(err.message, false);
    }
});

document.getElementById("form-login").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    try {
        const data = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: fd.get("email"),
                password: fd.get("password"),
            }),
        });
        setSession(data.token, data.role);
        showFlash("Добро пожаловать", true);
        showView("tasks");
        loadTasks();
    } catch (err) {
        showFlash(err.message, false);
    }
});

async function loadSkillOptions(selectEl, selectedIds) {
    const skills = await api("/api/skills");
    selectEl.innerHTML = "";
    skills.forEach((s) => {
        const opt = document.createElement("option");
        opt.value = s.id;
        opt.textContent = s.name + (s.category ? " (" + s.category + ")" : "");
        if (selectedIds && selectedIds.includes(s.id)) opt.selected = true;
        selectEl.appendChild(opt);
    });
}

function fillTaskFormMeta() {
    const typeSel = document.querySelector("#form-task select[name=taskType]");
    typeSel.innerHTML = TASK_TYPES.map(
        (t) => `<option value="${t}">${t}</option>`,
    ).join("");
    const prSel = document.querySelector("#form-task select[name=priority]");
    prSel.innerHTML =
        `<option value="">по умолчанию</option>` +
        PRIORITIES.map((p) => `<option value="${p}">${p}</option>`).join("");
}

document.getElementById("form-task").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const sel = fd.getAll("requiredSkillIds");
    const ids = sel.map((x) => Number(x)).filter(Boolean);
    const payload = {
        title: fd.get("title"),
        description: fd.get("description") || null,
        taskType: fd.get("taskType"),
        priority: fd.get("priority") || null,
        requiredSkillIds: ids.length ? ids : null,
    };
    try {
        await api("/api/tasks", {method: "POST", body: JSON.stringify(payload)});
        showFlash("Задача создана", true);
        e.target.reset();
        fillTaskFormMeta();
        await loadSkillOptions(document.getElementById("task-skills"), []);
        loadTasks();
    } catch (err) {
        showFlash(err.message, false);
    }
});

let currentTaskId = null;

async function loadTasks() {
    updateNav();
    fillTaskFormMeta();
    await loadSkillOptions(document.getElementById("task-skills"), []);
    try {
        const tasks = await api("/api/tasks");
        const ul = document.getElementById("task-list");
        ul.innerHTML = "";
        tasks.forEach((t) => {
            const li = document.createElement("li");
            const a = document.createElement("a");
            a.href = "#";
            a.textContent = t.title + " — " + t.status;
            a.addEventListener("click", (ev) => {
                ev.preventDefault();
                openTaskDetail(t.id);
            });
            li.appendChild(a);
            ul.appendChild(li);
        });
    } catch (err) {
        showFlash(err.message, false);
    }
}

async function openTaskDetail(id) {
    currentTaskId = id;
    showView("task-detail");
    const box = document.getElementById("task-detail");
    box.innerHTML = "";
    document.getElementById("task-matches-wrap").hidden = true;
    document.getElementById("task-apply-wrap").hidden = true;
    document.getElementById("task-apps-wrap").hidden = true;
    try {
        const t = await api("/api/tasks/" + id);
        box.innerHTML = `
      <div class="card">
        <h2>${escapeHtml(t.title)}</h2>
        <p>${escapeHtml(t.description || "")}</p>
        <p><small>Тип: ${t.taskType} · Статус: ${t.status} · Автор: ${escapeHtml(t.createdByName || "")}</small></p>
        <p><small>Навыки: ${(t.requiredSkills || []).map((s) => s.name).join(", ") || "—"}</small></p>
      </div>`;

        let me = null;
        if (token()) {
            try {
                cachedMe = null;
                me = await api("/api/me");
                cachedMe = me;
            } catch {
                /* ignore */
            }
        }
        const isAuthor = me && me.id === t.createdById;
        document.getElementById("task-matches-wrap").hidden = false;
        document.getElementById("task-apps-wrap").hidden = !isAuthor;

        if (role() === "VOLUNTEER") {
            document.getElementById("task-apply-wrap").hidden = false;
        }

        if (token()) {
            try {
                const matches = await api("/api/tasks/" + id + "/matches");
                const mul = document.getElementById("task-matches");
                mul.innerHTML = matches.length
                    ? matches
                          .map(
                              (m) =>
                                  `<li><strong>${escapeHtml(m.fullName)}</strong><br/>
              ${escapeHtml(m.email)} · ${escapeHtml(m.phone || "")}<br/>
              Рейтинг: ${m.rating ?? "—"} · Совпадения по навыкам: ${(m.matchedSkills || []).join(", ") || "—"}
              </li>`,
                          )
                          .join("")
                    : "<li>Подходящих волонтёров не найдено по текущим фильтрам.</li>";
            } catch {
                document.getElementById("task-matches").innerHTML =
                    "<li>Совпадения доступны автору задачи после входа.</li>";
            }
            if (isAuthor) {
                try {
                    const apps = await api("/api/tasks/" + id + "/applications");
                    document.getElementById("task-apps").innerHTML = apps.length
                        ? apps
                              .map(
                                  (a) =>
                                      `<li>${escapeHtml(a.volunteerName)} — ${escapeHtml(a.volunteerEmail)}<br/><small>${escapeHtml(a.message || "")}</small></li>`,
                              )
                              .join("")
                        : "<li>Откликов пока нет.</li>";
                } catch {
                    document.getElementById("task-apps").innerHTML =
                        "<li>Не удалось загрузить отклики.</li>";
                }
            }
        } else {
            document.getElementById("task-matches").innerHTML =
                "<li>Войдите как автор задачи, чтобы видеть контакты и список подходящих волонтёров.</li>";
        }
    } catch (err) {
        showFlash(err.message, false);
    }
}

document.getElementById("task-detail-back").addEventListener("click", () => {
    showView("tasks");
    loadTasks();
});

document.getElementById("form-apply").addEventListener("submit", async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    try {
        await api("/api/tasks/" + currentTaskId + "/applications", {
            method: "POST",
            body: JSON.stringify({message: fd.get("message") || null}),
        });
        showFlash("Отклик отправлен", true);
        e.target.reset();
    } catch (err) {
        showFlash(err.message, false);
    }
});

async function loadProfile() {
    updateNav();
    const viewBox = document.getElementById("profile-view");
    const form = document.getElementById("form-profile");
    if (!token()) {
        viewBox.innerHTML = "<p>Войдите, чтобы открыть кабинет.</p>";
        form.hidden = true;
        return;
    }
    try {
        cachedMe = null;
        const p = await api("/api/me");
        viewBox.innerHTML = `
      <p><strong>${escapeHtml(p.fullName)}</strong> (${p.role})</p>
      <p>${escapeHtml(p.email)}</p>
      <p>Телефон: ${escapeHtml(p.phone || "—")}</p>
      <p>Рейтинг: ${p.rating ?? "—"} · Часы: ${p.volunteerHours ?? "—"}</p>
      <p>Навыки: ${(p.skills || []).map((s) => s.name).join(", ") || "—"}</p>`;
        form.hidden = false;
        form.fullName.value = p.fullName;
        form.phone.value = p.phone || "";
        const vf = document.getElementById("profile-volunteer-fields");
        if (p.role === "VOLUNTEER") {
            vf.hidden = false;
            const selected = (p.skills || []).map((s) => s.id);
            await loadSkillOptions(document.getElementById("profile-skills"), selected);
            const pr = p.preferences;
            form.worksWithCats.checked = pr?.worksWithCats !== false;
            form.worksWithDogs.checked = pr?.worksWithDogs !== false;
            form.worksWithShelters.checked = pr?.worksWithShelters !== false;
            form.worksWithPrivate.checked = pr?.worksWithPrivate !== false;
        } else {
            vf.hidden = true;
        }
    } catch (err) {
        showFlash(err.message, false);
    }
}

document.getElementById("form-profile").addEventListener("submit", async (e) => {
    e.preventDefault();
    const f = e.target;
    const payload = {
        fullName: f.fullName.value || null,
        phone: f.phone.value || null,
    };
    if (role() === "VOLUNTEER") {
        const ids = Array.from(f.skillIds.selectedOptions).map((o) => Number(o.value));
        payload.skillIds = ids;
        payload.preferences = {
            worksWithCats: f.worksWithCats.checked,
            worksWithDogs: f.worksWithDogs.checked,
            worksWithShelters: f.worksWithShelters.checked,
            worksWithPrivate: f.worksWithPrivate.checked,
        };
    }
    try {
        await api("/api/me", {method: "PATCH", body: JSON.stringify(payload)});
        showFlash("Профиль сохранён", true);
        loadProfile();
    } catch (err) {
        showFlash(err.message, false);
    }
});

function escapeHtml(s) {
    if (!s) return "";
    return String(s)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;");
}

updateNav();
showView("home");
