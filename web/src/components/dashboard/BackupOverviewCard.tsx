import {
    Box,
    Card,
    CardContent,
    Chip,
    Grid,
    LinearProgress,
    Typography,
} from "@mui/material";
import type { JottaStatus } from "../../types/types";
import { myDate } from "../../utils/utils";

interface Props {
  data: JottaStatus | null;
}

export function BackupOverviewCard({ data }: Props) {
  const backups = data?.Backup?.State?.Enabled?.Backups ?? [];

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Backup‑oversikt
        </Typography>

        <Grid container spacing={2}>
          {backups.map((b) => {
            const errors = b.Errors ? Object.keys(b.Errors).length : 0;
            const uploadingFiles = b.Uploading?.Files ?? 0;
            const color =
              errors === 0 ? "success" : errors < 5 ? "warning" : "error";

            // Progress‑beregning
            const totalBytes = b.Count?.Bytes ?? 0;

            const remainingBytes = b.Uploading?.Bytes ?? 0;

            const uploadedBytes = totalBytes - remainingBytes;

            const percent =
              totalBytes > 0
                ? Math.round((uploadedBytes / totalBytes) * 100)
                : 0;

            return (
              <Grid
                size={{
                  xs: 12,
                  md: 6,
                  lg: 4,
                }}
                key={b.Path}
              >
                <Box
                  sx={{
                    border: "1px solid",
                    borderColor: "divider",
                    borderRadius: 2,
                    p: 2,
                    display: "flex",
                    flexDirection: "column",
                    gap: 1,
                  }}
                >
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                    }}
                  >
                    <Typography variant="subtitle1">{b.Path}</Typography>

                    <Typography variant="body2" color="text.secondary">
                      Totalt: {b.Count?.Files ?? 0} filer —{" "}
                      {Math.round((b.Count?.Bytes ?? 0) / 1024 / 1024)} MB
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      display: "flex",
                      flexDirection: "row",
                      gap: 1,
                      flexWrap: "wrap",
                    }}
                  >
                    <Chip label={`Feil: ${errors}`} color={color} />

                    {uploadingFiles > 0 && (
                      <Chip
                        label={`Laster opp ${uploadingFiles} filer`}
                        color="info"
                      />
                    )}
                  </Box>

                  {/* Progress‑bar vises kun når uploading pågår */}
                  {uploadingFiles > 0 && (
                    <Box sx={{ width: "100%", mt: 1 }}>
                      <LinearProgress
                        variant="determinate"
                        value={percent}
                        sx={{ height: 8, borderRadius: 1 }}
                      />
                      <Typography variant="caption" color="text.secondary">
                        {percent}% — {Math.round(remainingBytes / 1024 / 1024)}{" "}
                        MB gjenstår
                      </Typography>
                    </Box>
                  )}

                  <Typography variant="body2">
                    Sist oppdatert:{" "}
                    {b.LastUpdateMS ? myDate(b.LastUpdateMS) : "ukjent"}
                  </Typography>

                  <Typography variant="body2">
                    Neste backup:{" "}
                    {b.NextBackupMS ? myDate(b.NextBackupMS) : "ukjent"}
                  </Typography>
                </Box>
              </Grid>
            );
          })}
        </Grid>
      </CardContent>
    </Card>
  );
}
