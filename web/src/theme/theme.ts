import { createTheme } from "@mui/material/styles";

export const makeTheme = (mode: "light" | "dark") =>
    createTheme({
        palette: {
            mode,
            primary: {
                main: "#9b4dff", // vakker lilla
            },
        },
        components: {
            MuiCssBaseline: {
                styleOverrides: {
                    body: {
                        transition: "background-color 0.35s ease, color 0.35s ease",
                    },
                    "*": {
                        transition: "background-color 0.25s ease, color 0.25s ease, border-color 0.25s ease",
                    }
                }
            }
        }
    });
