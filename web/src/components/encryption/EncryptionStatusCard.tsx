import { Alert, Box, Chip, Stack, Typography } from "@mui/material";
import type { EncryptionState, EncryptionStatus } from "../../types/types";

export function EncryptionStatusCard({ status }: { status: EncryptionStatus }) {
    const errorStates: EncryptionState[] = ["FAILED", "REJECTED"];
    const warningStates: EncryptionState[] = ["TEARDOWN", "RESTORING", "MANUAL_OVERRIDE", "NOT_INITIALIZED"];

    return (
        <Box p={2} borderRadius={2} sx={{
            flex: 1,
            border: 1,
            borderColor: "divider",
            borderRadius: 2,
            p: 2,
            display: "flex",
            flexDirection: "column"
        }}>
            <Typography variant="h6" gutterBottom>
                Status
            </Typography>

            <Stack spacing={2}>
                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="body1">Tilstand:</Typography>
                    <Chip
                        label={status.state}
                        color={
                            errorStates.includes(status.state)
                                ? "error"
                                : warningStates.includes(status.state)
                                    ? "warning"
                                    : "success"
                        }
                    />
                </Stack>

                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="body1">Algoritme:</Typography>
                    <Typography variant="body2" color="text.secondary">
                        {status.algorithm}
                    </Typography>
                </Stack>

                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="body1">Montert:</Typography>
                    <Chip
                        label={status.mounted ? "Ja" : "Nei"}
                        color={status.mounted ? "success" : "default"}
                        variant="outlined"
                    />
                </Stack>

                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="body1">Backend:</Typography>
                    <Chip
                        label={status.backendExists ? "OK" : "Mangler"}
                        color={status.backendExists ? "success" : "error"}
                        variant="outlined"
                    />
                </Stack>

                {status.reason && (
                    <Alert severity="error">{status.reason}</Alert>
                )}
            </Stack>
        </Box>
    );
}
