import { Box, Card, CardContent, Typography } from "@mui/material";
import type { JottaStatus } from "../../types/types";

interface Props {
    data: JottaStatus | null;
}

export function ActivityHistoryCard({ data }: Props) {
    const backups = data?.Backup?.State?.Enabled?.Backups ?? [];
    const history = backups.flatMap((b) => b.History ?? []).slice(0, 10);

    return (
        <Card>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Aktivitet & historikk
                </Typography>

                {history.length === 0 && (
                    <Typography variant="body2" color="text.secondary">
                        Ingen historikk tilgjengelig.
                    </Typography>
                )}

                {history.map((h, i) => (
                    <Box key={i} sx={{ mb: 1 }}>
                        <Typography variant="body2">
                            {h.Path} — {h.Finished ? "Fullført" : "Pågår"}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Start: {h.Started ? new Date(h.Started).toLocaleString() : "?"}
                            {" — "}
                            Slutt: {h.Ended ? new Date(h.Ended).toLocaleString() : "?"}
                        </Typography>
                    </Box>
                ))}
            </CardContent>
        </Card>
    );
}
