let currentPage = 0;
const pageSize = 5;
let selectedIndex = -1;
let debounceTimer = null;

/* =====================================================
   SEARCH
===================================================== */

function search(page = 0) {
    clearTimeout(debounceTimer);

    debounceTimer = setTimeout(() => {
        executeSearch(page);
    }, 250); // debounce for better UX
}

function executeSearch(page) {
    currentPage = page;
    selectedIndex = -1;

    const queryInput = document.getElementById("query");
    const domainInput = document.getElementById("domain");
    const resultsDiv = document.getElementById("results");
    const loader = document.getElementById("loader");
    const searchBtn = document.getElementById("searchBtn");

    const query = queryInput.value.trim();
    const domain = domainInput ? domainInput.value.trim() : "";

    if (!query) return;

    showSkeleton(resultsDiv);
    loader.classList.remove("hidden");

    if (searchBtn) {
        searchBtn.disabled = true;
        searchBtn.textContent = "Searching…";
    }

    let url = `/search?q=${encodeURIComponent(query)}&page=${page}&size=${pageSize}`;
    if (domain) url += `&domain=${encodeURIComponent(domain)}`;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            loader.classList.add("hidden");
            resetButton(searchBtn);

            saveRecentSearch(query);
            renderResults(data, query);
        })
        .catch(() => {
            loader.classList.add("hidden");
            resetButton(searchBtn);
            resultsDiv.innerHTML = "<p>Something went wrong. Try again.</p>";
        });
}

function resetButton(btn) {
    if (!btn) return;
    btn.disabled = false;
    btn.textContent = "Search";
}

/* =====================================================
   RESULTS
===================================================== */

function renderResults(data, query) {
    const resultsDiv = document.getElementById("results");
    resultsDiv.innerHTML = "";

    if (!data.results || data.results.length === 0) {
        resultsDiv.innerHTML = "<p>No results found</p>";
        return;
    }

    data.results.forEach(item => {
        const div = document.createElement("div");
        div.className = "result";

        const timeAgo = item.crawlTime
            ? timeSince(new Date(item.crawlTime).getTime())
            : "recent";

        const isFresh =
            item.crawlTime &&
            Date.now() - new Date(item.crawlTime).getTime() < 3600_000;

        const favicon = `https://www.google.com/s2/favicons?domain=${item.domain}&sz=32`;

        div.innerHTML = `
            <a href="#" onclick="openInTab('${item.url}', '${item.title.replace(/'/g, "")}')">
                <img src="${favicon}" class="favicon" alt="">
                ${highlight(item.title, query)}
                ${isFresh ? `<span class="badge">🔥 Fresh</span>` : ""}
            </a>

            <div class="meta">
                🌐 ${item.domain} · ⏱ ${timeAgo}
            </div>

            <p>${highlight(item.snippet, query)}</p>
        `;

        resultsDiv.appendChild(div);
    });

    renderPagination(data.totalResults);
}

/* =====================================================
   PAGINATION
===================================================== */

function renderPagination(totalResults) {
    const resultsDiv = document.getElementById("results");
    const totalPages = Math.ceil(totalResults / pageSize);
    if (totalPages <= 1) return;

    const nav = document.createElement("div");
    nav.className = "pagination";

    if (currentPage > 0) {
        const prev = document.createElement("button");
        prev.textContent = "← Previous";
        prev.onclick = () => search(currentPage - 1);
        nav.appendChild(prev);
    }

    if (currentPage < totalPages - 1) {
        const next = document.createElement("button");
        next.textContent = "Next →";
        next.onclick = () => search(currentPage + 1);
        nav.appendChild(next);
    }

    resultsDiv.appendChild(nav);
}

/* =====================================================
   UTILITIES
===================================================== */

function highlight(text, query) {
    if (!text) return "";
    const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    return text.replace(new RegExp(`(${escaped})`, "gi"), "<mark>$1</mark>");
}

function timeSince(timestamp) {
    const seconds = Math.floor((Date.now() - timestamp) / 1000);
    const units = [
        { s: 31536000, l: "year" },
        { s: 2592000, l: "month" },
        { s: 86400, l: "day" },
        { s: 3600, l: "hour" },
        { s: 60, l: "minute" }
    ];

    for (const u of units) {
        const v = Math.floor(seconds / u.s);
        if (v >= 1) return `${v} ${u.l}${v > 1 ? "s" : ""} ago`;
    }
    return "just now";
}

/* =====================================================
   RECENT SEARCHES
===================================================== */

function saveRecentSearch(query) {
    let list = JSON.parse(localStorage.getItem("recentSearches")) || [];
    list = list.filter(q => q !== query);
    list.unshift(query);
    localStorage.setItem("recentSearches", JSON.stringify(list.slice(0, 5)));
    renderRecentSearches();
}

function renderRecentSearches() {
    const container = document.getElementById("recentSearches");
    if (!container) return;

    const list = JSON.parse(localStorage.getItem("recentSearches")) || [];
    container.innerHTML = "";

    list.forEach(q => {
        const chip = document.createElement("span");
        chip.textContent = q;
        chip.onclick = () => {
            document.getElementById("query").value = q;
            search(0);
        };
        container.appendChild(chip);
    });
}

/* =====================================================
   KEYBOARD NAVIGATION
===================================================== */

document.addEventListener("keydown", e => {
    const results = document.querySelectorAll(".result");
    if (!results.length) return;

    if (e.key === "ArrowDown") {
        e.preventDefault();
        selectedIndex = (selectedIndex + 1) % results.length;
        updateSelection(results);
    }

    if (e.key === "ArrowUp") {
        e.preventDefault();
        selectedIndex = (selectedIndex - 1 + results.length) % results.length;
        updateSelection(results);
    }

    if (e.key === "Enter" && selectedIndex >= 0) {
        const link = results[selectedIndex].querySelector("a");
        if (link) window.open(link.href, "_blank");
    }
});

function updateSelection(results) {
    results.forEach((el, i) =>
        el.classList.toggle("selected", i === selectedIndex)
    );

    results[selectedIndex].scrollIntoView({
        block: "nearest",
        behavior: "smooth"
    });
}

/* =====================================================
   UI ENHANCEMENTS
===================================================== */

function showSkeleton(container) {
    container.innerHTML = "";
    for (let i = 0; i < 3; i++) {
        const sk = document.createElement("div");
        sk.className = "result skeleton";
        sk.innerHTML = "<div class='sk-title'></div><div class='sk-text'></div>";
        container.appendChild(sk);
    }
}

/* =====================================================
   INIT
===================================================== */

document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("query");
    input?.focus();
    renderRecentSearches();

    input?.addEventListener("keydown", e => {
        if (e.key === "Enter") {
            e.preventDefault();
            search(0);
        }
    });
});

/* tab ui logic*/
let tabs = [];
let activeTab = null;

function openInTab(url, title) {
    const tabBar = document.getElementById("tabBar");
    const tabContent = document.getElementById("tabContent");
    const iframe = document.getElementById("tabFrame");

    tabBar.classList.remove("hidden");
    tabContent.classList.remove("hidden");

    // Check if already open
    const existing = tabs.find(t => t.url === url);
    if (existing) {
        activateTab(existing.id);
        return;
    }

    const id = Date.now();
    const tab = { id, url, title };

    tabs.push(tab);

    const tabEl = document.createElement("div");
    tabEl.className = "tab";
    tabEl.id = `tab-${id}`;

    tabEl.innerHTML = `
        <span>${title.substring(0, 20)}</span>
        <button onclick="closeTab(${id})">✕</button>
    `;

    tabEl.onclick = () => activateTab(id);
    tabBar.appendChild(tabEl);

    activateTab(id);
}

function activateTab(id) {
    const iframe = document.getElementById("tabFrame");
    const fallback = document.getElementById("iframeFallback");
    const openBtn = document.getElementById("openExternalBtn");

    const tab = tabs.find(t => t.id === id);
    if (!tab) return;

    // Highlight active tab
    tabs.forEach(t => {
        document
            .getElementById(`tab-${t.id}`)
            ?.classList.toggle("active", t.id === id);
    });

    activeTab = id;

    // Reset UI
    fallback.classList.add("hidden");
    iframe.style.display = "block";
    iframe.src = "about:blank";

    // Load iframe
    iframe.src = tab.url;

    // Detect iframe block (reliable method)
    setTimeout(() => {
        try {
            iframe.contentWindow.location.href;
        } catch (e) {
            showFallback(tab.url);
        }
    }, 800);

    iframe.onerror = () => showFallback(tab.url);

    function showFallback(url) {
        iframe.style.display = "none";
        fallback.classList.remove("hidden");
        openBtn.onclick = () => window.open(url, "_blank");
    }
}



function closeTab(id) {
    tabs = tabs.filter(t => t.id !== id);
    document.getElementById(`tab-${id}`)?.remove();

    if (activeTab === id && tabs.length > 0) {
        activateTab(tabs[tabs.length - 1].id);
    }

    if (tabs.length === 0) {
        document.getElementById("tabBar").classList.add("hidden");
        document.getElementById("tabContent").classList.add("hidden");
        document.getElementById("tabFrame").src = "";
    }
}

/* =====================================================
   THEME TOGGLE
===================================================== */

function initThemeToggle() {
    const themeToggle = document.getElementById("themeToggle");
    if (!themeToggle) return;

    // Check for saved theme preference or default to light mode
    const savedTheme = localStorage.getItem("theme") || "light";
    applyTheme(savedTheme);

    themeToggle.addEventListener("click", () => {
        const currentTheme = document.body.classList.contains("dark") ? "dark" : "light";
        const newTheme = currentTheme === "dark" ? "light" : "dark";
        applyTheme(newTheme);
        localStorage.setItem("theme", newTheme);
    });
}

function applyTheme(theme) {
    const themeToggle = document.getElementById("themeToggle");
    if (theme === "dark") {
        document.body.classList.add("dark");
        if (themeToggle) themeToggle.textContent = "☀️ Light Mode";
    } else {
        document.body.classList.remove("dark");
        if (themeToggle) themeToggle.textContent = "🌙 Dark Mode";
    }
}

// Initialize theme toggle on page load
document.addEventListener("DOMContentLoaded", initThemeToggle);
