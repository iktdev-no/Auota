import { Box, Card, CardContent, Chip, Grid, Typography } from "@mui/material";
import type { JottaStatus } from "../../types/types";

interface Props {
    data: JottaStatus | null;
}

export function BackupOverviewCard({ data }: Props) {
    const backups = data?.Backup?.State?.Enabled?.Backups ?? [];

    return (
        <Card>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Backup‑oversikt
                </Typography>

                <Grid container spacing={2}>
                    {backups.map((b) => {
                        const errors = b.Errors ? Object.keys(b.Errors).length : 0;
                        const color =
                            errors === 0 ? "success" : errors < 5 ? "warning" : "error";

                        return (
                            <Grid size={{
                                xs: 12,
                                md: 6,
                                lg: 4
                            }} key={b.Path}>
                                <Box
                                    sx={{
                                        border: "1px solid",
                                        borderColor: "divider",
                                        borderRadius: 2,
                                        p: 2,
                                        display: "flex",
                                        flexDirection: "column",
                                        gap: 1
                                    }}
                                >
                                    <Typography variant="subtitle1">{b.Path}</Typography>

                                    <Chip label={`Feil: ${errors}`} color={color} />

                                    <Typography variant="body2">
                                        Sist oppdatert:{" "}
                                        {b.LastUpdateMS
                                            ? new Date(b.LastUpdateMS).toLocaleString()
                                            : "ukjent"}
                                    </Typography>

                                    <Typography variant="body2">
                                        Neste backup:{" "}
                                        {b.NextBackupMS
                                            ? new Date(b.NextBackupMS).toLocaleString()
                                            : "ukjent"}
                                    </Typography>
                                </Box>
                            </Grid>
                        );
                    })}
                </Grid>
            </CardContent>
        </Card>
    );
}
