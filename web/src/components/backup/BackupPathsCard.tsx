import { Card, CardContent, Divider, Stack, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { getBackupFolders } from "../../api/backup";
import type { BackupItem } from "../../types/types";
import EmptyFolder from "../files/EmptyFolderCard";
import { BackupPathItem } from "./BackupPathItem";

export function BackupPathsCard() {
    const [paths, setPaths] = useState<BackupItem[]>([]);

    const load = () => {
        getBackupFolders().then((res) => setPaths(res ?? []));
    };

    useEffect(() => {
        load();
    }, []);

    return (
        <Card>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Backup‑oppsett
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Stack spacing={1}>
                    {paths.map((item) => (
                        <BackupPathItem key={item.path} item={item} reload={load} />
                    ))}
                </Stack>

                {paths.length === 0 && (
                    <EmptyFolder text="Ingen backup plasseringer er konfigurert" />
                )}
            </CardContent>
        </Card>
    );
}
