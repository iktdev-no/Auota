import { Card, CardContent, Typography } from "@mui/material";
import type { EncryptionState } from "../../types/types";
import { EncryptionWipeDialog } from "./EncryptionWipeDialog";

export function EncryptionResetCard({ currentState }: { currentState: EncryptionState }) {
    return (
        <Card>
            <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                    Tilbakestill krypteringskonfigurasjon
                </Typography>

                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Sletter gocryptfs‑konfigurasjonen og reinitialiserer systemet.
                </Typography>

                <EncryptionWipeDialog isEnabled={currentState == "NOT_ENABLED"} />
            </CardContent>
        </Card>
    );
}
