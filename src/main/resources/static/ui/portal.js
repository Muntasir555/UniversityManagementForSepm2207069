
"use strict";

const $ = id => document.getElementById(id);

const role = document.body.dataset.role;
const token = sessionStorage.getItem("ums_token");

const resource = role === "STUDENT"
    ? "students"
    : "teachers";

let profile = null;

// Go to login if no token exists.
if (!token) {
    window.location.replace("/ui/index.html");
} else {
    loadProfile();
}

// SEND AUTHENTICATED API REQUESTS
async function api(path, options = {}) {
    const response = await fetch(path, {
        ...options,
        headers: {
            Authorization: "Bearer " + token,
            ...(options.body
                ? { "Content-Type": "application/json" }
                : {})
        }
    });

    if (response.status === 401) {
        sessionStorage.removeItem("ums_token");
        window.location.replace("/ui/index.html");
        throw new Error("Login expired");
    }

    if (response.status === 403) {
        throw new Error(
            "Access denied for this account"
        );
    }

    if (!response.ok) {
        throw new Error(
            "Request failed: " + response.status
        );
    }

    return response.json();
}

// LOAD CURRENT USER PROFILE
async function loadProfile() {
    try {
        profile = await api("/api/" + resource + "/me");

        $("summaryName").textContent = profile.fullName;
        $("summaryDepartment").textContent =
            profile.department;

        $("summaryExtra").textContent =
            role === "STUDENT"
                ? profile.semester
                : profile.designation;

        $("profileId").textContent = profile.id;
        $("profileName").textContent = profile.fullName;
        $("profileUsername").textContent = profile.username;
        $("profileEmail").textContent = profile.email;
        $("profileDepartment").textContent =
            profile.department;

        $("profileExtra").textContent =
            role === "STUDENT"
                ? profile.semester
                : profile.designation;

    } catch (error) {
        $("message").textContent = error.message;
    }
}

// OPEN PROFILE EDIT FORM
$("editBtn").addEventListener("click", () => {
    if (!profile) return;

    $("fullName").value = profile.fullName;
    $("email").value = profile.email;
    $("department").value = profile.department;

    $("extra").value = role === "STUDENT"
        ? profile.semester
        : profile.designation;

    $("editSection").classList.remove("hidden");
});

// CANCEL PROFILE EDIT
$("cancelBtn").addEventListener("click", () => {
    $("editSection").classList.add("hidden");
});

// SAVE PROFILE CHANGES
$("editForm").addEventListener("submit", async event => {
    event.preventDefault();

    if (!profile) return;

    const body = {
        fullName: $("fullName").value.trim(),
        email: $("email").value.trim(),
        department: $("department").value.trim()
    };

    if (role === "STUDENT") {
        body.semester = Number($("extra").value);
    } else {
        body.designation = $("extra").value.trim();
    }

    try {
        await api("/api/" + resource + "/" + profile.id, {
            method: "PUT",
            body: JSON.stringify(body)
        });

        await loadProfile();

        $("editSection").classList.add("hidden");

        $("message").textContent =
            "Profile updated successfully!";

    } catch (error) {
        $("message").textContent = error.message;
    }
});

// SIDEBAR NAVIGATION
$("sidebarProfile").addEventListener("click", () => {
    $("profileSection").scrollIntoView({
        behavior: "smooth"
    });
});

// LOGOUT
$("logoutBtn").addEventListener("click", () => {
    sessionStorage.removeItem("ums_token");
    window.location.replace("/ui/index.html");
});
