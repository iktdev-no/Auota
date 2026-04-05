import { Box, Card, CardContent, Typography } from "@mui/material";
import type { JottaStatus } from "../../types/types";
import { myDate } from "../../utils/utils";

interface Props {
  data: JottaStatus | null;
}

export function ActivityHistoryCard({ data }: Props) {
  const backups = data?.Backup?.State?.Enabled?.Backups ?? [];
  const history = backups.flatMap((b) => b.History ?? []).slice(0, 10);

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Aktivitet & historikk
        </Typography>

        {history.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            Ingen historikk tilgjengelig.
          </Typography>
        )}

        {history.map((h, i) => (
          <Box key={i} sx={{ mb: 1 }}>
            <Typography variant="body2">
              {h.Path} — {h.Finished || h.Ended ? "Fullført" : "Pågår"}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Start: {h.Started ? myDate(h.Started) : "?"}
              {" — "}
              Slutt: {h.Ended ? myDate(h.Ended) : "?"}
            </Typography>
            {h.Finished ||
              (h.Ended && (
                <Typography variant="caption" color="text.secondary">
                  Antall filer:{" "}
                  {h.Upload?.Completed?.Files ? h.Upload?.Completed?.Files : 0}
                </Typography>
              ))}
          </Box>
        ))}
      </CardContent>
    </Card>
  );
}
