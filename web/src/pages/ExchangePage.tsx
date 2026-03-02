import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import {
    Accordion,
    AccordionDetails,
    AccordionSummary,
    Box,
    Card,
    CardContent,
    Chip,
    Divider,
    LinearProgress,
    Stack,
    Typography
} from "@mui/material";
import { useEffect, useState } from "react";
import { downloadLogs, uploadLogs } from "../api/transfer";
import type { JottaTransfer } from "../types/types";

export default function ExchangePage() {
    const [uploads, setUploads] = useState<JottaTransfer[]>([]);
    const [downloads, setDownloads] = useState<JottaTransfer[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                const up = await uploadLogs();
                const down = await downloadLogs();
                setUploads(up);
                setDownloads(down);
            } finally {
                setLoading(false);
            }
        };
        void load();
    }, []);

    const renderTransfer = (t: JottaTransfer) => {
        const totalBytes = t.Total?.Bytes ?? 0;
        const remainingBytes = t.Remaining?.Bytes ?? 0;
        const done = remainingBytes === 0;
        const progress = totalBytes > 0 ? ((totalBytes - remainingBytes) / totalBytes) * 100 : 0;

        return (
            <Card key={t.Id} sx={{ mb: 2 }}>
                <CardContent>
                    <Stack direction="row" justifyContent="space-between" alignItems="center">
                        <Typography variant="h6">{t.Id}</Typography>
                        <Chip
                            label={done ? "Ferdig" : "Pågår"}
                            color={done ? "success" : "warning"}
                            size="small"
                        />
                    </Stack>

                    <Typography variant="body2" sx={{ mt: 1 }}>
                        <strong>Remote:</strong> {t.Remote}
                    </Typography>
                    <Typography variant="body2">
                        <strong>Local:</strong> {t.Local}
                    </Typography>

                    <Box sx={{ mt: 2 }}>
                        <LinearProgress variant="determinate" value={progress} />
                        <Typography variant="caption">
                            {done ? "100%" : `${progress.toFixed(1)}%`}
                        </Typography>
                    </Box>

                    <Divider sx={{ my: 2 }} />

                    <Typography variant="body2">
                        <strong>Filer:</strong> {t.Total?.Files ?? 0}
                    </Typography>
                    <Typography variant="body2">
                        <strong>Størrelse:</strong> {t.Total?.Bytes ?? 0} bytes
                    </Typography>

                    {t.CompletedTimeMs && (
                        <Typography variant="body2" sx={{ mt: 1 }}>
                            <strong>Fullført:</strong>{" "}
                            {new Date(t.CompletedTimeMs).toLocaleString()}
                        </Typography>
                    )}
                </CardContent>
            </Card>
        );
    };

    const splitTransfers = (list: JottaTransfer[]) => {
        const active = list.filter(t => (t.Remaining?.Bytes ?? 0) > 0);
        const done = list.filter(t => (t.Remaining?.Bytes ?? 0) === 0);
        return { active, done };
    };

    const uploadsSplit = splitTransfers(uploads);
    const downloadsSplit = splitTransfers(downloads);

    return (
        <Box sx={{ height: "100%", width: "100%", display: "flex", flexDirection: "column" }}>
            <Box sx={{ p: 3, overflow: "auto" }}>
                <Typography variant="h4" sx={{ mb: 3 }}>
                    Filutveksling
                </Typography>

                {/* UPLOADS */}
                <Typography variant="h5" sx={{ mb: 1 }}>
                    Opplastinger
                </Typography>

                <Accordion defaultExpanded>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>Aktive ({uploadsSplit.active.length})</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {uploadsSplit.active.length === 0 && (
                            <Typography variant="body2">Ingen aktive opplastinger.</Typography>
                        )}
                        {uploadsSplit.active.map(renderTransfer)}
                    </AccordionDetails>
                </Accordion>

                <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>Ferdige ({uploadsSplit.done.length})</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {uploadsSplit.done.length === 0 && (
                            <Typography variant="body2">Ingen ferdige opplastinger.</Typography>
                        )}
                        {uploadsSplit.done.map(renderTransfer)}
                    </AccordionDetails>
                </Accordion>

                <Divider sx={{ my: 4 }} />

                {/* DOWNLOADS */}
                <Typography variant="h5" sx={{ mb: 1 }}>
                    Nedlastinger
                </Typography>

                <Accordion defaultExpanded>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>Aktive ({downloadsSplit.active.length})</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {downloadsSplit.active.length === 0 && (
                            <Typography variant="body2">Ingen aktive nedlastinger.</Typography>
                        )}
                        {downloadsSplit.active.map(renderTransfer)}
                    </AccordionDetails>
                </Accordion>

                <Accordion>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography>Ferdige ({downloadsSplit.done.length})</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        {downloadsSplit.done.length === 0 && (
                            <Typography variant="body2">Ingen ferdige nedlastinger.</Typography>
                        )}
                        {downloadsSplit.done.map(renderTransfer)}
                    </AccordionDetails>
                </Accordion>
            </Box>
        </Box>
    );
}
