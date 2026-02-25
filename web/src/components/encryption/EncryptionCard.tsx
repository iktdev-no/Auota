import { useState } from "react";
import {
    toggleEncryption,
    updateEncryptionPassword
} from "../../api/encryption";

import {
    Box,
    Card,
    CardContent,
    CircularProgress,
    Divider,
    Stack,
    Tooltip,
    Typography
} from "@mui/material";

import { useEncryptionStatus } from "../../status/EncryptionStatusProvider";
import { ExportStoreButton } from "./EncryptionExport";
import { EncryptionImportDialog } from "./EncryptionImportDialog";
import { EncryptionManualMountCard } from "./EncryptionManualMountCard";
import { EncryptionOverrideToggleCard } from "./EncryptionOverrideCard";
import { EncryptionPasswordDialog } from "./EncryptionPasswordDialog";
import { EncryptionPasswordSection } from "./EncryptionPasswordSection";
import { EncryptionResetCard } from "./EncryptionResetCard";
import { EncryptionStatusCard } from "./EncryptionStatusCard";
import { EncryptionToggle } from "./EncryptionToggle";

export function EncryptionCard() {
    const status = useEncryptionStatus();
    const [loading, setLoading] = useState(false);
    const [passwordDialogOpen, setPasswordDialogOpen] = useState(false);

    async function handleToggle() {
        if (!status) return;
        setLoading(true);
        await toggleEncryption(!status.enabled);
        setLoading(false);
        // Ingen setStatus – SSE oppdaterer automatisk
    }

    async function handlePasswordSave(password: string) {
        setLoading(true);
        await updateEncryptionPassword(password);
        setPasswordDialogOpen(false);
        setLoading(false);
        // Ingen setStatus – SSE oppdaterer automatisk
    }

    if (!status) {
        return (
            <Card>
                <CardContent>
                    <Box sx={{ display: "flex", justifyContent: "center", py: 2 }}>
                        <CircularProgress size={24} />
                    </Box>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardContent>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 1 }}>
                    <Typography variant="h6">Kryptering</Typography>

                    <Tooltip title={status.passwordSet ? "" : "Du må sette et passord før du kan aktivere kryptering"}>
                        <span>
                            <EncryptionToggle
                                enabled={status.enabled}
                                loading={loading}
                                onToggle={handleToggle}
                                disabled={!status.passwordSet || loading}
                            />
                        </span>
                    </Tooltip>
                </Box>

                <Divider sx={{ mb: 2 }} />

                <Stack spacing={3}>
                    <EncryptionPasswordSection
                        enabled={status.enabled}
                        passwordIncorrect={status.passwordIncorrect}
                        passwordSet={status.passwordSet}
                        onOpenDialog={() => setPasswordDialogOpen(true)}
                    />

                    <EncryptionStatusCard status={status} />
                </Stack>

                <Divider sx={{ my: 2 }} />
                <EncryptionOverrideToggleCard />
                {status.state === "MANUAL_OVERRIDE" && (
                    <EncryptionManualMountCard />
                )}
                <Divider sx={{ my: 2 }} />

                <Typography variant="subtitle1">Backup og Gjennoppretting</Typography>

                <Box sx={{ display: "flex", gap: 2, mt: 2, flexDirection: { xs: "column", md: "row" } }}>
                    <Box sx={{ flex: 1, border: 1, borderColor: "divider", borderRadius: 2, p: 2 }}>
                        <Typography variant="subtitle2">Eksporter nøkkel</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Last ned en sikkerhetskopi av krypteringsnøkkelen.
                        </Typography>
                        {status.exportable ? <ExportStoreButton /> : (
                            <Typography variant="caption" color="error">
                                Ingen nøkkel tilgjengelig for eksport.
                            </Typography>
                        )}
                    </Box>

                    <Box sx={{ flex: 1, border: 1, borderColor: "divider", borderRadius: 2, p: 2 }}>
                        <Typography variant="subtitle2">Importer nøkkel</Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Gjenopprett krypteringsnøkkelen fra en tidligere eksportert fil.
                        </Typography>
                        <EncryptionImportDialog />
                    </Box>
                </Box>

                <EncryptionResetCard currentState={status.state} />

                <EncryptionPasswordDialog
                    hasExportableSetupInstructions={status.exportable}
                    isChangingPassword={status.passwordSet}
                    open={passwordDialogOpen}
                    onClose={() => setPasswordDialogOpen(false)}
                    onSave={handlePasswordSave}
                />
            </CardContent>
        </Card>
    );
}

