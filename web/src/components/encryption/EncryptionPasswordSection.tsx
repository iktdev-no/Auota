import { Box, Button, Chip, Stack, Typography } from "@mui/material";

export function EncryptionPasswordSection({
    enabled,
    passwordSet,
    passwordIncorrect,
    onOpenDialog
}: {
    enabled: boolean,
    passwordSet: boolean;
    passwordIncorrect: boolean;
    onOpenDialog: () => void;
}) {
    return (
        <Stack spacing={1}>
            {/* Top row: label + chip on left, button on right */}
            <Box
                sx={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between"
                }}
            >
                <Stack direction="row" spacing={2} alignItems="center">
                    <Typography variant="body1">Passord:</Typography>
                    <Chip
                        label={passwordSet ? "Satt" : "Ikke satt"}
                        color={passwordSet ? "success" : "warning"}
                        variant="outlined"
                    />
                    {passwordIncorrect && (
                        <Chip
                            label="Feil passord"
                            color="error"
                            variant="outlined"
                        />
                    )}

                </Stack>

                <Button disabled={enabled} variant="contained" onClick={onOpenDialog}>
                    {passwordSet ? "Endre passord" : "Sett passord"}
                </Button>
            </Box>

            {/* Note under */}
            {!passwordSet && (
                <Typography variant="body2" color="text.secondary">
                    Kryptering kan ikke aktiveres før et passord er satt.
                </Typography>
            )}
        </Stack>
    );
}
