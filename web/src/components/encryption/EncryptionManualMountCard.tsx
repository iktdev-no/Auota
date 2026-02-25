import { Button, Card, CardContent, Stack, Typography } from "@mui/material";
import { manualMount, manualUnmount } from "../../api/encryption";
import { useEncryptionStatus } from "../../status/EncryptionStatusProvider";

export function EncryptionManualMountCard() {
    const status = useEncryptionStatus();

    return (
        <Card>
            <CardContent>
                <Typography variant="subtitle1" gutterBottom>
                    Manuell mount / unmount
                </Typography>

                <Stack direction="row" spacing={2}>
                    <Button
                        variant="outlined"
                        onClick={() => manualMount()}
                        disabled={status?.mounted}
                    >
                        Mount
                    </Button>

                    <Button
                        variant="outlined"
                        color="warning"
                        onClick={() => manualUnmount()}
                        disabled={!status?.mounted}
                    >
                        Unmount
                    </Button>
                </Stack>
            </CardContent>
        </Card>
    );
}
