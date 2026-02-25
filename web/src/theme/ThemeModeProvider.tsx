import { CssBaseline, ThemeProvider } from "@mui/material";
import { createContext, useContext, useMemo, useState, type PropsWithChildren } from "react";
import { makeTheme } from "./theme";

type Mode = "light" | "dark";

type ThemeModeContextValue = {
    mode: Mode;
    toggle: () => void;
    setMode: (m: Mode) => void;
};

const ThemeModeContext = createContext<ThemeModeContextValue | null>(null);

function loadTheme(): Mode {
    const saved = localStorage.getItem("theme-mode");
    return saved === "dark" ? "dark" : "light";
}

function saveTheme(mode: Mode) {
    localStorage.setItem("theme-mode", mode);
}

export function ThemeModeProvider({ children }: PropsWithChildren) {
    const [mode, setModeState] = useState<Mode>(loadTheme());

    const setMode = (m: Mode) => {
        saveTheme(m);
        setModeState(m);
    };

    const toggle = () => setMode(mode === "light" ? "dark" : "light");

    const theme = useMemo(() => makeTheme(mode), [mode]);

    return (
        <ThemeModeContext.Provider value={{ mode, toggle, setMode }}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {children}
            </ThemeProvider>
        </ThemeModeContext.Provider>
    );
}

export function useThemeMode() {
    const ctx = useContext(ThemeModeContext);
    if (!ctx) throw new Error("useThemeMode must be used inside ThemeModeProvider");
    return ctx;
}
