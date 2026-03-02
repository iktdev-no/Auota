import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import FolderIcon from "@mui/icons-material/Folder";
import { Box, Container, Divider, Grid, ListItemIcon, ListItemText, Typography } from "@mui/material";
import type { JSX } from "react";
import type { Roots, RootType } from "../../types/types";

export interface RootFolderComponentProps {
    roots: Roots[];
    loading: boolean;
    error: string | null;
    onOpenRoot: (root: Roots) => void;
}

export default function RootFolderComponent(props: RootFolderComponentProps): JSX.Element {
    const { roots, loading, error, onOpenRoot } = props;

    const mainFolders: Roots[] = roots.filter((r) => r.type !== "LocalFolder");
    const localFolders: Roots[] = roots.filter((r) => r.type === "LocalFolder");

    return (
        <Container
            sx={{
                display: "flex",
                height: "calc(100% - 70px)",
                alignItems: "center",
                justifyContent: "center"
            }}
            maxWidth="lg"
        >
            <Grid container spacing={3} direction="column" alignItems="center">

                {loading && (
                    <Typography sx={{ mb: 2 }}>Laster…</Typography>
                )}

                {error && (
                    <Typography color="error" sx={{ mb: 2 }}>
                        {error}
                    </Typography>
                )}

                {/* MAIN FOLDERS */}
                <Grid sx={{ xs: 12 }}>
                    <Grid container spacing={8} justifyContent="center">
                        {mainFolders.map((root) => (
                            <Grid
                                key={root.id}
                                sx={{ cursor: "pointer" }}
                                onClick={() => onOpenRoot(root)}
                            >
                                <Box sx={{ textAlign: "center" }}>
                                    <ListItemIcon>{getIcon(root.type)}</ListItemIcon>
                                    <ListItemText primary={root.name} />
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>

                <Divider sx={{ width: "100%", my: 2 }} />

                {/* LOCAL FOLDERS */}
                <Grid sx={{ xs: 12 }}>
                    <Grid container spacing={8} justifyContent="center">
                        {localFolders.map((root) => (
                            <Grid
                                key={root.id}
                                sx={{ cursor: "pointer" }}
                                onClick={() => onOpenRoot(root)}
                            >
                                <Box sx={{ textAlign: "center" }}>
                                    <ListItemIcon>{getIcon(root.type)}</ListItemIcon>
                                    <ListItemText primary={root.name} />
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>
            </Grid>
        </Container>
    );
}

function getIcon(type: RootType): JSX.Element {
    const size = 64;

    switch (type) {
        case "Jotta":
            return (
                <FolderIcon
                    sx={{
                        fontSize: size,
                        color: "#a654fc"
                    }}
                />
            );

        case "UploadEncrypted":
            return (
                <Box sx={{ position: "relative", display: "inline-block" }}>
                    <FolderIcon sx={{ fontSize: size, color: "#1976d2" }} />
                    <CloudUploadIcon
                        sx={{
                            fontSize: size / 2,
                            position: "absolute",
                            top: size / 4,
                            right: size / 4
                        }}
                    />
                </Box>
            );

        case "Download":
            return (
                <Box sx={{ position: "relative", display: "inline-block" }}>
                    <FolderIcon sx={{ fontSize: size, color: "#29991c" }} />
                    <CloudDownloadIcon
                        sx={{
                            fontSize: size / 2,
                            position: "absolute",
                            top: size / 4,
                            right: size / 4
                        }}
                    />
                </Box>
            );

        default:
            return (
                <FolderIcon
                    sx={{
                        fontSize: size,
                        color: "#fbc02d"
                    }}
                />
            );
    }
}
