import { Card, CardContent, CircularProgress, Grid, Typography } from "@mui/material";
import { useSystemHealth } from "../hooks/useSystemHealth";

export function HealthPage() {
    const { health, loading } = useSystemHealth();

    if (loading || !health) {
        return (
            <div style={{ display: "flex", justifyContent: "center", padding: 40 }}>
                <CircularProgress />
            </div>
        );
    }


    return (
        <Grid container spacing={2} sx={{ p: 2 }}>
            <Grid size={{ sm: 12 }}>
                <Typography variant="h4">Systemstatus</Typography>
            </Grid>

            <Grid size={{ sm: 12 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6">Kryptering</Typography>
                        <Typography>Status: {health.encryption}</Typography>
                        <Typography>Backend finnes: {health.backendExists ? "Ja" : "Nei"}</Typography>
                        <Typography>Montert: {health.mounted ? "Ja" : "Nei"}</Typography>
                    </CardContent>
                </Card>
            </Grid>

            <Grid size={{ sm: 12 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6">Autentisering</Typography>
                        <Typography>Status: {health.auth}</Typography>
                    </CardContent>
                </Card>
            </Grid>


            <Grid size={{ sm: 12 }}>
                <Card>
                    <CardContent>
                        <Typography variant="h6">Jottad</Typography>
                        <Typography>Status: {health.jottad}</Typography>
                    </CardContent>
                </Card>
            </Grid>

            <Grid size={{ sm: 12 }}>
                <Typography variant="caption">
                    Sist oppdatert: {new Date(health.lastUpdated).toLocaleString()}
                </Typography>
            </Grid>
        </Grid>
    );
}
