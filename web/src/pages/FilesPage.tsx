import { Box, Stack, Typography } from "@mui/material";
import { useCallback, useEffect, useState, type JSX } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { addBackup, ignoreBackupOrFolder, removeBackup, unignoreBackupOrFolder } from "../api/backup";
import { apiGet } from "../api/client";
import { downloadFileOrFolder, uploadFileOrFolder } from "../api/transfer";
import BreadcrumbPath from "../components/BreadcrumbPath";
import RootFolderComponent from "../components/files/RootFolderComponent";
import UnifiedExplorer, { type RootKind } from "../components/files/UnifiedExplorer";
import { LoadingToast } from "../components/LoadingToast";
import type { FileAction, Roots } from "../types/types";
import type { UnifiedFile } from "../types/webtypes";

export default function FilesPage(): JSX.Element {
    const { root } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const [roots, setRoots] = useState<Roots[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // Determine if we are at /files (root view)
    const isRootView = !root;

    // Extract path from URL
    const effectiveRoot: RootKind = (root as RootKind) ?? "local";
    const path = location.pathname.replace(`/files/${effectiveRoot}`, "") || "/";

    const goTo = useCallback(
        (root: RootKind, nextPath: string): void => {
            navigate(`/files/${root}${nextPath}`);
        },
        [navigate]
    );

    const goToRoot = useCallback(() => {
        navigate("/files");
    }, [navigate]);


    useEffect(() => {
        const loadRoots = async (): Promise<void> => {
            try {
                setLoading(true);
                setError(null);
                const data = await apiGet<Roots[]>("/files/roots");
                setRoots(data);
            } catch (e) {
                console.error(e);
                setError("Kunne ikke laste rotene.");
            } finally {
                setLoading(false);
            }
        };
        loadRoots();
    }, []);

    if (isRootView) {
        return (
            <Box sx={{ height: "100%", width: "100%", display: "flex", flexDirection: "column" }}>
                <RootFolderComponent
                    roots={roots}
                    loading={loading}
                    error={error}
                    onOpenRoot={(r) => {
                        const nextRoot: RootKind = r.type === "Jotta" ? "jotta" : "local";
                        goTo(nextRoot, r.path);
                    }}
                />
            </Box>
        );
    }

    const onFileAction = async (action: FileAction, file: UnifiedFile) => {
        console.log("FILE ACTION:", action, file);

        switch (action.id) {
            case "AddToBackup":
                await addBackup(file.uri);   // <— return value ignored
                break;

            case "RemoveFromBackup":
                await removeBackup(file.uri); // <— return value ignored
                break;

            case "IncludeInBackup":
                await unignoreBackupOrFolder({ backupRoot: null, exclude: file.uri })
                console.warn("IncludeInBackup not implemented yet");
                break;

            case "ExcludeFromBackup":
                await ignoreBackupOrFolder({ backupRoot: null, exclude: file.uri })
                break;

            case "Upload":
                await uploadFileOrFolder(file.uri);
                break;

            case "Download":
                await downloadFileOrFolder(file.uri)
                break;
            case "Open":
                if (file.type === "Folder") {
                    //load(file.uri);
                    return;
                }
                break;
        }

    };

    return (
        <Box sx={{ height: "100%", width: "100%", display: "flex", flexDirection: "column" }}>
            <Box
                sx={{
                    position: "sticky",
                    top: 0,
                    zIndex: 20,
                    pt: 1,
                    bgcolor: "background.paper",
                    borderBottom: 1,
                    borderColor: "divider",
                }}
            >
                <Stack direction="row" alignItems="center" spacing={2} p={1}>
                    <BreadcrumbPath root={effectiveRoot} path={path} onRoot={goToRoot} onNavigate={goTo} />
                </Stack>
            </Box>

            <UnifiedExplorer root={effectiveRoot} path={path} onNavigate={goTo} onFileAction={onFileAction} />

            <LoadingToast open={loading} />
            {error && (
                <Typography color="error" sx={{ p: 2 }}>
                    {error}
                </Typography>
            )}
        </Box>
    );
}
