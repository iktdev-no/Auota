export function loadThemeMode(): "light" | "dark" {
    const saved = localStorage.getItem("theme-mode");
    return saved === "dark" ? "dark" : "light";
}

export function saveThemeMode(mode: "light" | "dark") {
    localStorage.setItem("theme-mode", mode);
}
