
"use strict";

// ==========================================
// GLOBAL VARIABLES
// ==========================================

// Admin JWT is kept in memory on this page.
let token = null;
let currentRole = null;
let currentType = "students";

let students = [];
let teachers = [];

const $ = (id) => document.getElementById(id);

// ==========================================
// UI HELPERS
// ==========================================

function show(id, visible) {
    $(id).classList.toggle("hidden", !visible);
}

function message(text, success = false) {
    $("appMessage").textContent = text;
    $("appMessage").classList.toggle("success", success);
}

// ==========================================
// READ ROLE FROM JWT
// ==========================================

// This only reads JWT claims for UI navigation.
// The Spring Boot backend verifies the JWT.
function getRole(jwt) {
    const parts = jwt.split(".");

    if (parts.length !== 3) {
        throw new Error("Invalid login token");
    }

    const base64 = parts[1]
        .replace(/-/g, "+")
        .replace(/_/g, "/");

    const claims = JSON.parse(
        atob(
            base64.padEnd(
                Math.ceil(base64.length / 4) * 4,
                "="
            )
        )
    );

    return claims.role;
}

// ==========================================
// AUTHENTICATED API REQUESTS
// ==========================================

async function api(path, options = {}) {

    const headers = {
        ...(options.body
            ? { "Content-Type": "application/json" }
            : {}),

        ...(token
            ? { "Authorization": "Bearer " + token }
            : {})
    };

    const response = await fetch(path, {
        ...options,
        headers
    });

    // JWT expired or invalid.
    if (response.status === 401) {
        logout();

        throw new Error(
            "Your login has expired. Please sign in again."
        );
    }

    if (!response.ok) {
        const errorText = await response.text();

        throw new Error(
            `Request failed (${response.status}). ` +
            errorText.slice(0, 180)
        );
    }

    // DELETE requests normally return 204.
    if (response.status === 204) {
        return null;
    }

    return response.json();
}

// ==========================================
// LOGIN - ALL THREE ROLES
// ==========================================

$("loginForm").addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        $("loginMessage").textContent = "";

        const username = $("username").value.trim();
        const password = $("password").value;

        try {
            // Login without an existing JWT.
            const response = await fetch(
                "/api/auth/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify({
                        username,
                        password
                    })
                }
            );

            if (!response.ok) {
                throw new Error(
                    "Invalid username or password"
                );
            }

            const data = await response.json();

            if (!data.accessToken) {
                throw new Error(
                    "The server did not return a JWT"
                );
            }

            // Store issued JWT and identify role.
            token = data.accessToken;
            currentRole = getRole(token);

            $("password").value = "";

            // ==================================
            // STUDENT LOGIN
            // ==================================

            if (currentRole === "STUDENT") {

                // Share token with the Student page
                // within the current browser tab.
                sessionStorage.setItem(
                    "ums_token",
                    token
                );

                window.location.replace(
                    "/ui/student.html"
                );

                return;
            }

            // ==================================
            // TEACHER LOGIN
            // ==================================

            if (currentRole === "TEACHER") {

                // Share token with the Teacher page.
                sessionStorage.setItem(
                    "ums_token",
                    token
                );

                window.location.replace(
                    "/ui/teacher.html"
                );

                return;
            }

            // ==================================
            // ADMIN LOGIN
            // ==================================

            if (currentRole === "ADMIN") {

                // Admin does not need the separate
                // Student/Teacher portal token.
                sessionStorage.removeItem("ums_token");

                show("loginScreen", false);
                show("dashboard", true);
                show("logoutBtn", true);

                $("roleLabel").textContent = "ADMIN";

                $("welcomeText").textContent =
                    "Welcome, " + username;

                show("adminPanel", true);
                show("otherRolePanel", false);

                await refreshData();

                return;
            }

            // Reject unexpected roles.
            throw new Error(
                "Your account has an unsupported role"
            );

        } catch (error) {

            // Clear transient authentication state
            // when login or role processing fails.
            token = null;
            currentRole = null;

            sessionStorage.removeItem("ums_token");

            $("loginMessage").textContent =
                error.message;
        }
    }
);

// ==========================================
// LOGOUT
// ==========================================

function logout() {

    // Clear Admin authentication.
    token = null;
    currentRole = null;

    // Also clear portal authentication.
    sessionStorage.removeItem("ums_token");

    students = [];
    teachers = [];

    show("dashboard", false);
    show("loginScreen", true);
    show("logoutBtn", false);
    show("adminPanel", false);
    show("otherRolePanel", false);

    $("loginForm").reset();
}

$("logoutBtn").addEventListener(
    "click",
    logout
);

// ==========================================
// LOAD ADMIN DATA
// ==========================================

async function refreshData() {

    if (currentRole !== "ADMIN") {
        return;
    }

    try {

        const results = await Promise.all([
            api("/api/students"),
            api("/api/teachers")
        ]);

        students = results[0];
        teachers = results[1];

        $("studentCount").textContent =
            students.length;

        $("teacherCount").textContent =
            teachers.length;

        renderTable("students");
        renderTable("teachers");

        message("");

    } catch (error) {
        message(error.message);
    }
}

// ==========================================
// RENDER STUDENTS / TEACHERS TABLE
// ==========================================

function renderTable(type) {

    const table = type === "students"
        ? $("studentsTable")
        : $("teachersTable");

    const records = type === "students"
        ? students
        : teachers;

    table.replaceChildren();

    for (const record of records) {

        const row = document.createElement("tr");

        const values = type === "students"
            ? [
                record.id,
                record.fullName,
                record.email,
                record.department,
                record.semester
            ]
            : [
                record.id,
                record.fullName,
                record.email,
                record.department,
                record.designation
            ];

        for (const value of values) {

            const cell = document.createElement("td");

            // textContent avoids interpreting record
            // values as HTML.
            cell.textContent = value ?? "";

            row.appendChild(cell);
        }

        // ACTION BUTTONS
        const actions = document.createElement("td");

        const edit = document.createElement("button");

        edit.textContent = "Edit";
        edit.className = "action-button";

        edit.addEventListener("click", () => {
            openForm(type, record);
        });

        const remove = document.createElement("button");

        remove.textContent = "Delete";
        remove.className =
            "action-button danger";

        remove.addEventListener("click", () => {
            deleteRecord(type, record);
        });

        actions.append(edit, remove);

        row.appendChild(actions);
        table.appendChild(row);
    }
}

// ==========================================
// ADMIN DASHBOARD TABS
// ==========================================

document.querySelectorAll("[data-tab]")
    .forEach(button => {

        button.addEventListener("click", () => {

            const type = button.dataset.tab;

            show(
                "studentsSection",
                type === "students"
            );

            show(
                "teachersSection",
                type === "teachers"
            );

            document.querySelectorAll("[data-tab]")
                .forEach(tab => {

                    tab.classList.toggle(
                        "active",
                        tab.dataset.tab === type
                    );
                });
        });
    });

// ==========================================
// OPEN ADD / EDIT FORM
// ==========================================

function openForm(type, record = null) {

    currentType = type;

    $("recordForm").reset();

    $("recordId").value =
        record ? record.id : "";

    $("formMessage").textContent = "";

    $("dialogTitle").textContent =
        (record ? "Edit " : "Add ") +
        (type === "students"
            ? "Student"
            : "Teacher");

    show(
        "semesterField",
        type === "students"
    );

    show(
        "designationField",
        type === "teachers"
    );

    show(
        "credentialsFields",
        !record
    );

    $("recordUsername").required = !record;
    $("recordPassword").required = !record;

    $("recordSemester").required =
        type === "students";

    $("recordDesignation").required =
        type === "teachers";

    if (record) {

        $("recordName").value =
            record.fullName;

        $("recordEmail").value =
            record.email;

        $("recordDepartment").value =
            record.department;

        if (type === "students") {

            $("recordSemester").value =
                record.semester;

        } else {

            $("recordDesignation").value =
                record.designation;
        }
    }

    $("recordDialog").showModal();
}

// ==========================================
// ADD STUDENT / TEACHER BUTTONS
// ==========================================

$("addStudentBtn").addEventListener(
    "click",
    () => {
        openForm("students");
    }
);

$("addTeacherBtn").addEventListener(
    "click",
    () => {
        openForm("teachers");
    }
);

$("cancelDialog").addEventListener(
    "click",
    () => {
        $("recordDialog").close();
    }
);

// ==========================================
// CREATE OR UPDATE RECORD
// ==========================================

$("recordForm").addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        const id = $("recordId").value;
        const editing = Boolean(id);

        const data = {

            fullName:
                $("recordName").value.trim(),

            email:
                $("recordEmail").value.trim(),

            department:
                $("recordDepartment").value.trim()
        };

        if (currentType === "students") {

            data.semester = Number(
                $("recordSemester").value
            );

        } else {

            data.designation =
                $("recordDesignation").value.trim();
        }

        if (!editing) {

            data.username =
                $("recordUsername").value.trim();

            data.password =
                $("recordPassword").value;
        }

        const path =
            "/api/" + currentType +
            (editing ? "/" + id : "");

        try {

            await api(path, {

                method: editing
                    ? "PUT"
                    : "POST",

                body: JSON.stringify(data)
            });

            $("recordDialog").close();

            await refreshData();

            if (token) {

                message(
                    (editing
                        ? "Updated "
                        : "Created ") +
                    "successfully!",
                    true
                );
            }

        } catch (error) {

            $("formMessage").textContent =
                error.message;
        }
    }
);

// ==========================================
// DELETE STUDENT / TEACHER
// ==========================================

async function deleteRecord(type, record) {

    const confirmed = confirm(
        "Delete " + record.fullName + "? " +
        "This will remove the linked login account too."
    );

    if (!confirmed) {
        return;
    }

    try {

        await api(
            "/api/" + type + "/" + record.id,
            {
                method: "DELETE"
            }
        );

        await refreshData();

        if (token) {
            message(
                "Record deleted successfully!",
                true
            );
        }

    } catch (error) {

        message(error.message);
    }
}
