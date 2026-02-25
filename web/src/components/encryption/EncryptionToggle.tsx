import { Stack, Switch, Typography } from "@mui/material";

export function EncryptionToggle({
    enabled,
    loading,
    disabled,
    onToggle
}: {
    enabled: boolean;
    loading: boolean;
    disabled?: boolean;
    onToggle: () => void;
}) {
    return (
        <Stack direction="row" spacing={1} alignItems="center">
            <Typography variant="body2">Aktivert</Typography>
            <Switch
                checked={enabled}
                onChange={onToggle}
                disabled={loading || disabled}
            />
        </Stack>
    );
}
