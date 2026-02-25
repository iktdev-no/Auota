import {
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField,
    Typography
} from "@mui/material";
import { useEffect, useState } from "react";
import { answerLogout, poll, startLogout } from "../../api/auth";
import type { AuthStep } from "../../types/types";

interface LogoutDialogProps {
    open: boolean;
    onClose: () => void;
}

export function LogoutDialog({ open, onClose }: LogoutDialogProps) {
    const [sessionId, setSessionId] = useState<string | null>(null);
    const [prompt, setPrompt] = useState<string>("");
    const [step, setStep] = useState<AuthStep>("UNKNOWN");
    const [input, setInput] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        if (!open) return;

        const start = async () => {
            setLoading(true);
            const data = await startLogout();

            setSessionId(data.sessionId);
            setPrompt(data.message);
            setStep(data.step);
            setInput("");
            setLoading(false);

            if (data.step === "DONE") onClose();
        };

        start();
    }, [open, onClose]);

    useEffect(() => {
        if (step !== "WAIT" || !sessionId) return;

        const id = setInterval(async () => {
            const data = await poll(sessionId);

            setPrompt(data.message);
            setStep(data.step);
            setSessionId(data.sessionId);

            if (data.step === "DONE") {
                clearInterval(id);
                onClose();
            }
        }, 1000);

        return () => clearInterval(id);
    }, [step, sessionId, onClose]);

    const sendAnswer = async (value: string) => {
        if (!sessionId) return;

        setLoading(true);

        const data = await answerLogout(sessionId, value);
        setLoading(false);

        setPrompt(data.message);
        setStep(data.step);
        setSessionId(data.sessionId);
        setInput("");

        if (data.step === "DONE") {
            onClose();
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Logg ut</DialogTitle>

            <DialogContent>
                <Typography sx={{ mb: 2, whiteSpace: "pre-line" }}>
                    {prompt}
                </Typography>

                {step === "WAIT" && (
                    <div style={{ display: "flex", justifyContent: "center", padding: 20 }}>
                        <CircularProgress />
                    </div>
                )}

                {step === "CONFIRM" && (
                    <>
                        <Button
                            variant="contained"
                            color="error"
                            onClick={() => sendAnswer("y")}
                            disabled={loading}
                            sx={{ mr: 1 }}
                        >
                            Ja, logg ut
                        </Button>

                        <Button
                            variant="outlined"
                            onClick={() => sendAnswer("n")}
                            disabled={loading}
                        >
                            Avbryt
                        </Button>
                    </>
                )}

                {step === "UNKNOWN" && (
                    <TextField
                        fullWidth
                        autoFocus
                        label="Svar"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        disabled={loading}
                    />
                )}

                {step === "ERROR" && (
                    <Typography color="error">
                        En feil oppstod: {prompt}
                    </Typography>
                )}
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose} disabled={loading}>
                    Lukk
                </Button>

                {step === "UNKNOWN" && (
                    <Button
                        onClick={() => sendAnswer(input)}
                        disabled={loading}
                    >
                        Send
                    </Button>
                )}
            </DialogActions>
        </Dialog>
    );
}
