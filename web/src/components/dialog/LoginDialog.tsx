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
import { answerLogin, poll, startLogin } from "../../api/auth";
import type { AuthStep } from "../../types/types";

interface LoginDialogProps {
    open: boolean;
    onClose: () => void;
}

export function LoginDialog({ open, onClose }: LoginDialogProps) {
    const [sessionId, setSessionId] = useState<string | null>(null);
    const [prompt, setPrompt] = useState<string>("");
    const [step, setStep] = useState<AuthStep>("UNKNOWN");
    const [input, setInput] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [hasPolled, setHasPolled] = useState<boolean>(false);

    useEffect(() => {
        if (!open) return;

        const start = async () => {
            setLoading(true);
            const data = await startLogin();

            setSessionId(data.sessionId);
            setPrompt(data.message);
            setStep(data.step);
            setInput("");
            setHasPolled(false);
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
            setHasPolled(true);

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

        const data = await answerLogin(sessionId, value);
        setLoading(false);

        setPrompt(data.message);
        setStep(data.step);
        setSessionId(data.sessionId);
        setInput("");

        if (data.step === "DONE") {
            onClose();
        }
    };

    const shouldShowInput =
        step === "PAT" ||
        step === "DEVICE_NAME" ||
        (step === "UNKNOWN" && hasPolled);


    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Logg inn</DialogTitle>

            <DialogContent>
                <Typography sx={{ mb: 2, whiteSpace: "pre-line" }}>
                    {prompt}
                </Typography>

                {step === "WAIT" && (
                    <div style={{ display: "flex", justifyContent: "center", padding: 20 }}>
                        <CircularProgress />
                    </div>
                )}

                {/* ⭐ LICENSE = JA/NEI-knapper */}
                {step === "LICENSE" && (
                    <>
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={() => sendAnswer("yes")}
                            disabled={loading}
                            sx={{ mr: 1 }}
                        >
                            Godta lisens
                        </Button>

                        <Button
                            variant="outlined"
                            color="error"
                            onClick={() => sendAnswer("no")}
                            disabled={loading}
                        >
                            Avslå
                        </Button>
                    </>
                )}

                {/* ⭐ Input kun når vi vet det trengs */}
                {shouldShowInput && (
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
                    Avbryt
                </Button>

                {shouldShowInput && (
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
