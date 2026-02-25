import {
    Alert,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Stack,
    TextField
} from "@mui/material";
import { useState } from "react";

export function EncryptionPasswordDialog({
    open,
    onClose,
    onSave,
    isChangingPassword,
    hasExportableSetupInstructions
}: {
    open: boolean;
    onClose: () => void;
    onSave: (password: string) => void;
    isChangingPassword: boolean;
    hasExportableSetupInstructions: boolean;
}) {
    const [password, setPassword] = useState("");

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>
                {isChangingPassword ? "Endre passord" : "Sett passord"}
            </DialogTitle>

            <DialogContent>
                <Stack spacing={2}>

                    {(isChangingPassword && hasExportableSetupInstructions) && (
                        <Alert severity="error">
                            Du er i ferd med å endre krypteringspassordet. Dette vil føre til at
                            systemet må reinitialisere og remounte det krypterte filsystemet.
                            <br />
                            <strong>
                                Hvis du mister dette passordet, vil du permanent miste tilgang til
                                alle krypterte data.
                            </strong>
                        </Alert>
                    )}

                    <TextField
                        autoFocus
                        margin="dense"
                        label={isChangingPassword ? "Nytt passord" : "Passord"}
                        type="password"
                        fullWidth
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </Stack>
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose}>Avbryt</Button>
                <Button
                    variant="contained"
                    onClick={() => onSave(password)}
                    disabled={!password}
                >
                    Lagre
                </Button>
            </DialogActions>
        </Dialog>
    );
}
