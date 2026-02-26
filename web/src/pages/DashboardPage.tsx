import { Box, Grid } from "@mui/material";
import { ActivityHistoryCard } from "../components/dashboard/ActivityHistoryCard";
import { BackupOverviewCard } from "../components/dashboard/BackupOverviewCard";
import { DaemonStatusCard } from "../components/dashboard/DaemonStatusCard";
import { UserCard } from "../components/dashboard/UserCard";
import { useJottaDaemonStatus } from "../status/JottaDaemonStatusProvider";
import { useJottaStatus } from "../status/JottaStatusProvider";

export function DashboardPage() {
    const status = useJottaStatus();
    const daemon = useJottaDaemonStatus();

    return (
        <Box
            sx={{
                p: 3,
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                gap: 3,
            }}
        >
            <Grid container spacing={3}>
                {/* TOP ROW */}
                <Grid size={{
                    sm: 8
                }}>
                    <UserCard data={status?.parsed ?? null} />
                </Grid>

                <Grid size={{
                    sm: 4
                }}>
                    <DaemonStatusCard
                        data={status?.parsed ?? null}
                        daemonState={daemon?.state}
                        pid={daemon?.pid ?? -1}
                    />
                </Grid>

                {/* BACKUP OVERVIEW */}
                <Grid size={{
                    sm: 12
                }}>
                    <BackupOverviewCard data={status?.parsed ?? null} />
                </Grid>

                {/* ACTIVITY HISTORY */}
                <Grid size={{
                    sm: 12
                }}>
                    <ActivityHistoryCard data={status?.parsed ?? null} />
                </Grid>
            </Grid>
        </Box>
    );
}
