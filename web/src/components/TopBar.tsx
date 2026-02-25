import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import MenuIcon from "@mui/icons-material/Menu";
import { AppBar, Box, IconButton, Toolbar, Typography } from "@mui/material";
import { useSseConnection } from "../sse/useSseConnection";
import { useThemeMode } from "../theme/ThemeModeProvider";

interface TopBarProps {
    onToggleSidebar: () => void;
}

export function TopBar({ onToggleSidebar }: TopBarProps) {
    const connected = useSseConnection();
    const { mode, toggle } = useThemeMode();

    return (
        <AppBar position="fixed" sx={{ zIndex: 1201 }}>
            <Toolbar>
                <IconButton
                    edge="start"
                    color="inherit"
                    onClick={onToggleSidebar}
                    sx={{ mr: 2 }}
                >
                    <MenuIcon />
                </IconButton>

                <Typography variant="h6" sx={{ flexGrow: 1 }}>
                    System Dashboard
                </Typography>

                {/* THEME TOGGLE */}
                <IconButton color="inherit" onClick={toggle} sx={{ mr: 2 }}>
                    {mode === "dark" ? <LightModeIcon /> : <DarkModeIcon />}
                </IconButton>

                {/* SSE STATUS */}
                <Box
                    sx={{
                        px: 2,
                        py: 0.5,
                        borderRadius: 1,
                        bgcolor: connected ? "success.main" : "error.main",
                        textTransform: "capitalize",
                    }}
                >
                    {connected ? "Connected" : "Disconnected"}
                </Box>
            </Toolbar>
        </AppBar>
    );
}
