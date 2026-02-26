import { Box, Card, CardContent, Chip, Typography } from "@mui/material";
import type { JottaStatus } from "../../types/types";

interface Props {
    data: JottaStatus | null;
    daemonState?: string;
    pid: number | null;
}

export function DaemonStatusCard({ data, daemonState, pid }: Props) {
    const uploading = data?.State?.Uploading;
    const downloading = data?.State?.Downloading;

    return (
        <Card>
            <CardContent>
                <Typography variant="h6" gutterBottom>
                    Jottad‑status
                </Typography>

                <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
                    <Chip
                        label={daemonState}
                        color={
                            daemonState === "RUNNING"
                                ? "success"
                                : daemonState === "STARTING"
                                    ? "warning"
                                    : "error"
                        }
                    />
                    <Chip label={`PID: ${pid ?? "?"}`} />
                </Box>

                <Box sx={{ mt: 2 }}>
                    <Typography variant="body2">
                        Opplastinger: {uploading ? Object.keys(uploading).length : 0}
                    </Typography>
                    <Typography variant="body2">
                        Nedlastinger: {downloading ? Object.keys(downloading).length : 0}
                    </Typography>
                </Box>
            </CardContent>
        </Card >
    );
}
