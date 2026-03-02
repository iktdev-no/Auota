import { Box, Typography } from "@mui/material";
import { useCallback, useEffect, useState } from "react";
import { apiGet } from "../../api/client";
import type { FileAction, IFile, JottaFs } from "../../types/types";
import type { UnifiedFile } from "../../types/webtypes";
import { mapJottaToUnified, mapLocalToUnified } from "../../utils/fileUtils";
import { LoadingToast } from "../LoadingToast";
import EmptyFolder from "./EmptyFolderCard";
import FileContextMenu from "./FileContextMenu";
import { UnifiedFileList } from "./UnifiedFileList";

export type RootKind = "root" | "local" | "jotta";

export interface UnifiedExplorerProps {
    root: RootKind;
    path: string;
    onNavigate: (root: RootKind, path: string) => void;
    onFileAction: (action: FileAction, file: UnifiedFile) => void;
}

export default function UnifiedExplorer(props: UnifiedExplorerProps) {
    const { root, path, onNavigate, onFileAction } = props;

    const [files, setFiles] = useState<UnifiedFile[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // Context menu state
    const [contextFile, setContextFile] = useState<UnifiedFile | null>(null);
    const [contextPos, setContextPos] = useState<{ mouseX: number; mouseY: number } | null>(null);

    const load = useCallback(
        async (p: string): Promise<void> => {
            try {
                setLoading(true);
                setError(null);

                if (root === "local") {
                    const data = await apiGet<IFile[]>(`/files/list?path=${encodeURIComponent(p)}`);
                    setFiles(data.map(mapLocalToUnified));
                } else {
                    const data = await apiGet<JottaFs>(`/files/jotta?path=${encodeURIComponent(p)}`);
                    setFiles(mapJottaToUnified(data));
                }
            } catch (e) {
                console.error(e);
                setError("Kunne ikke laste mappe");
            } finally {
                setLoading(false);
            }
        },
        [root]
    );

    useEffect(() => {
        void load(path);
    }, [path, load]);

    const handleContextMenu = (event: React.MouseEvent, file: UnifiedFile) => {
        event.preventDefault();
        setContextFile(file);
        setContextPos({ mouseX: event.clientX + 2, mouseY: event.clientY - 6 });
    };

    const closeContextMenu = () => {
        setContextPos(null);
        setContextFile(null);
    };

    if (error !== null) {
        return (
            <Typography color="error" sx={{ p: 2 }}>
                {error}
            </Typography>
        );
    }

    return (
        <Box sx={{ flex: 1, overflow: "auto", width: "100%" }}>
            {files.length === 0 && !loading ? (
                <EmptyFolder />
            ) : (
                <UnifiedFileList
                    files={files}
                    onOpenFolder={(file) => onNavigate(root, file.uri)}
                    onContextMenu={handleContextMenu}
                />
            )}

            <FileContextMenu
                file={contextFile}
                position={contextPos}
                onClose={closeContextMenu}
                onFileAction={onFileAction}
                onCopyPath={(file) => {
                    navigator.clipboard.writeText(file.uri);
                    closeContextMenu();
                }}
            />

            <LoadingToast open={loading} />
        </Box>
    );
}
