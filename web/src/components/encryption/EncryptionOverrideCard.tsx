import { Card, CardContent, FormControlLabel, Switch, Typography } from "@mui/material";
import { useState } from "react";
import { disableOverride, enableOverride } from "../../api/encryption";
import { useEncryptionStatus } from "../../status/EncryptionStatusProvider";

export function EncryptionOverrideToggleCard() {
    const status = useEncryptionStatus();
    const [loading, setLoading] = useState(false);

    if (!status) return null;

    const overrideEnabled = status.manualOverride;

    const handleToggle = async () => {
        if (loading) return;
        setLoading(true);

        try {
            if (overrideEnabled) {
                await disableOverride();
            } else {
                await enableOverride();
            }
        } catch (e) {

        }

        setLoading(false);
        // SSE oppdaterer status automatisk
    };

    return (
        <Card>
            <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                    Manuell override
                </Typography>

                <FormControlLabel
                    control={
                        <Switch
                            checked={overrideEnabled}
                            onChange={handleToggle}
                            disabled={loading || !status.enabled}
                        />
                    }
                    label={overrideEnabled ? "Override aktiv" : "Override deaktivert"}
                />
            </CardContent>
        </Card>
    );
}
