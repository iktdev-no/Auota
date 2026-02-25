import {
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Typography
} from "@mui/material";
import { useState } from "react";
import { deleteEncryptionStore } from "../../api/encryption";

export function EncryptionWipeDialog({ isEnabled }: { isEnabled: boolean }) {
    const [open, setOpen] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleDelete = async () => {
        setLoading(true);
        try {
            await deleteEncryptionStore();
            // Ingen onDone – SSE oppdaterer status automatisk
            setOpen(false);
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <Button
                disabled={!isEnabled}
                variant="outlined"
                color="error"
                onClick={() => setOpen(true)}
            >
                Slett krypteringsnøkkel
            </Button>

            <Dialog open={open} onClose={() => setOpen(false)}>
                <DialogTitle>Bekreft sletting</DialogTitle>
                <DialogContent>
                    <Typography variant="body2" color="text.secondary">
                        Dette vil slette gocryptfs‑konfigurasjonen.
                        <strong> Alle filer i backend vil fortsatt eksistere, men kan ikke dekrypteres uten en gyldig nøkkel.</strong>
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 2 }}>
                        Denne handlingen kan ikke angres.
                    </Typography>
                </DialogContent>

                <DialogActions>
                    <Button onClick={() => setOpen(false)}>Avbryt</Button>
                    <Button
                        variant="contained"
                        color="error"
                        onClick={handleDelete}
                        disabled={loading}
                    >
                        Ja, slett
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}
