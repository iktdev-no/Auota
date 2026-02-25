import { Box, Card, CardContent, Divider, Grid, Typography } from "@mui/material";
import { SseDot } from "../components/SseDot";
import { useJottaStatus } from "../status/JottaStatusProvider";

export function DashboardPage() {
    const status = useJottaStatus();

    return (
        <Box sx={{ p: 3, display: "flex", flexDirection: "column", gap: 3 }}>

            {/* Header */}
            <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                <Typography variant="h5" fontWeight={600}>
                    Dashboard
                </Typography>
                <SseDot />
            </Box>

            <Grid container spacing={3}>

                {/* STATUS CARD */}
                <Grid item xs={12} md={6}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Status
                            </Typography>
                            <Divider sx={{ mb: 2 }} />

                            {status ? (
                                <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
                                    <Typography variant="body1">
                                        <strong>Success:</strong> {status.success ? "Ja" : "Nei"}
                                    </Typography>

                                    <Typography variant="body1">
                                        <strong>Melding:</strong> {status.message ?? "Ingen melding"}
                                    </Typography>

                                    <Typography variant="body1">
                                        <strong>Raw:</strong>
                                    </Typography>
                                    <Box
                                        sx={{
                                            p: 1,
                                            backgroundColor: "background.paper",
                                            borderRadius: 1,
                                            fontFamily: "monospace",
                                            fontSize: "0.85rem",
                                            maxHeight: 150,
                                            overflow: "auto",
                                            border: "1px solid",
                                            borderColor: "divider",
                                        }}
                                    >
                                        {status.raw}
                                    </Box>
                                </Box>
                            ) : (
                                <Typography variant="body2" color="text.secondary">
                                    Laster status…
                                </Typography>
                            )}
                        </CardContent>
                    </Card>
                </Grid>

                {/* BACKUP CARD */}
                <Grid item xs={12} md={6}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Backup
                            </Typography>
                            <Divider sx={{ mb: 2 }} />

                            <Typography variant="body2" color="text.secondary">
                                Backup‑status kommer her (mapper, progresjon, siste kjøring…)
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* SYSTEM CARD */}
                <Grid item xs={12} md={6}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                System
                            </Typography>
                            <Divider sx={{ mb: 2 }} />

                            <Typography variant="body2" color="text.secondary">
                                Systeminfo kommer her (CPU, RAM, disk, versjon…)
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                {/* LOGG CARD */}
                <Grid item xs={12} md={6}>
                    <Card sx={{ height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" gutterBottom>
                                Logg
                            </Typography>
                            <Divider sx={{ mb: 2 }} />

                            <Typography variant="body2" color="text.secondary">
                                Siste logglinjer eller snarvei til logg‑siden.
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

            </Grid>
        </Box>
    );
}
