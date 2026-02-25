import AllInclusiveIcon from "@mui/icons-material/AllInclusive";
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Typography
} from "@mui/material";
import { useState } from "react";
import { LoginDialog } from "../components/dialog/LoginDialog";
import { useJottaStatus } from "../status/JottaStatusProvider";


import { BackupPathsCard } from "../components/backup/BackupPathsCard";
import { LogoutDialog } from "../components/dialog/LogoutDialog";
import { EncryptionCard } from "../components/encryption/EncryptionCard";
import { formatBytes } from "../utils";

export function SettingsPage() {
    const [loginOpen, setLoginOpen] = useState(false);
    const [logoutOpen, setLogoutOpen] = useState(false);
    const status = useJottaStatus();

    return (
        <Box sx={{
            display: "flex",
            flexDirection: "column",
            overflowY: "auto",
            height: "100%"
        }}>
            <Box
                sx={{
                    p: 3,
                    display: "flex",
                    flexDirection: "column",
                    gap: 3,
                }}
            >

                <Typography variant="h5" fontWeight={600}>
                    Innstillinger
                </Typography>

                {/* JOTTA ACCOUNT */}
                {status && (
                    <Card>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Jotta‑konto
                            </Typography>
                            <Divider sx={{ mb: 2 }} />
                            {status?.parsed ? (
                                <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
                                    <Typography>
                                        <strong>Innlogget som:</strong> {status?.parsed?.User?.Email}
                                    </Typography>



                                    <Typography>
                                        <strong>Quota:</strong>{" "}
                                        {formatBytes(status.parsed.User?.AccountInfo?.Usage ?? 0)} /{" "}
                                        {status.parsed.User?.AccountInfo?.Capacity === -1 ? (
                                            <AllInclusiveIcon
                                                sx={{
                                                    fontSize: "1.4rem",
                                                    verticalAlign: "middle",
                                                }}
                                            />
                                        ) : (
                                            formatBytes(status.parsed.User?.AccountInfo?.Capacity ?? 0)
                                        )}
                                    </Typography>




                                    <Button variant="outlined" color="error" sx={{ mt: 2 }} onClick={() => setLogoutOpen(true)}>
                                        Logg ut
                                    </Button>
                                </Box>
                            ) : (
                                <Box>
                                    <Typography variant="body2" color="text.secondary">
                                        Ikke innlogget.
                                    </Typography>
                                    <Button variant="contained" sx={{ mt: 2 }} onClick={() => setLoginOpen(true)}>
                                        Logg inn
                                    </Button>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                )}

                {/* ENCRYPTION */}
                <EncryptionCard />

                {/* BACKUP CONFIG */}
                <BackupPathsCard />

                {/* ABOUT */}
                <Card>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            Om Auota
                        </Typography>
                        <Divider sx={{ mb: 2 }} />

                        <Typography>Versjon: 1.0.0</Typography>
                        <Typography>Build: dev</Typography>
                        <Typography>Backend: Spring Boot</Typography>
                        <Typography>Frontend: React + MUI</Typography>
                    </CardContent>
                </Card>

                <LoginDialog open={loginOpen} onClose={() => setLoginOpen(false)} />
                <LogoutDialog open={logoutOpen} onClose={() => setLogoutOpen(false)} />
            </Box>
        </Box>
    );
}


