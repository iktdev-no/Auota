import { Box, Button, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { ignoreBackupOrFolder, removeBackup, unignoreBackupOrFolder } from "../../api/backup";
import type { BackupIEUpdate, BackupItem } from "../../types/types";
import { ConfirmDialog } from "../dialog/ConfirmationDialog";

interface Props {
    item: BackupItem;
    reload: () => void;
}

export function BackupPathItem({ item, reload }: Props) {
    const [confirmRemoveOpen, setConfirmRemoveOpen] = useState(false);
    const isRootIgnored = item.excludePaths.some((p) => p === item.path);

    const toggleRootIgnore = async () => {
        const body: BackupIEUpdate = {
            backupRoot: item.path,
            exclude: item.path
        };

        if (isRootIgnored) {
            await unignoreBackupOrFolder(body);
        } else {
            await ignoreBackupOrFolder(body);
        }
        reload();
    };

    const handleConfirmRemove = async () => {
        await removeBackup(item.path);
        setConfirmRemoveOpen(false);
        reload();
    };

    const toggleSubIgnore = async (sub: string) => {
        const body: BackupIEUpdate = {
            backupRoot: item.path,
            exclude: sub
        };

        const isIgnored = item.excludePaths.includes(sub);

        if (isIgnored) {
            await unignoreBackupOrFolder(body);
        } else {
            await ignoreBackupOrFolder(body);
        }
        reload();
    };

    return (
        <Box
            sx={{
                border: "1px solid",
                borderColor: "divider",
                borderRadius: 1,
                p: 1,
                display: "flex",
                flexDirection: "column",
                gap: 1
            }}
        >
            {/* ROOT HEADER */}
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center"
                }}
            >
                <Typography>{item.path}</Typography>

                <Stack direction="row" spacing={1}>
                    <Button
                        color={isRootIgnored ? "success" : "warning"}
                        onClick={toggleRootIgnore}
                    >
                        {isRootIgnored ? "Inkluder root" : "Ekskluder root"}
                    </Button>

                    <Button color="error" onClick={() => setConfirmRemoveOpen(true)}>
                        Fjern
                    </Button>

                </Stack>
            </Box>

            {/* SUB‑EXCLUDES */}
            {item.excludePaths
                .filter((p) => p !== item.path)
                .map((sub) => (
                    <Box
                        key={sub}
                        sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            pl: 2
                        }}
                    >
                        <Typography variant="body2">{sub}</Typography>

                        <Button
                            size="small"
                            color="success"
                            onClick={() => toggleSubIgnore(sub)}
                        >
                            Fjern ekskludering
                        </Button>
                    </Box>
                ))}
            <ConfirmDialog
                open={confirmRemoveOpen}
                title="Fjern backup‑root"
                message={`Er du sikker på at du vil fjerne '${item.path}' fra backup?`}
                confirmLabel="Fjern"
                cancelLabel="Avbryt"
                onConfirm={handleConfirmRemove}
                onCancel={() => setConfirmRemoveOpen(false)}
            />

        </Box>
    );
}
