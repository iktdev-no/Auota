// src/components/LogTail.tsx
import PauseIcon from "@mui/icons-material/Pause";
import PlayArrowIcon from "@mui/icons-material/PlayArrow";
import ReplayIcon from "@mui/icons-material/Replay";
import { Box, IconButton, Paper, Typography } from "@mui/material";
import { useEffect, useRef, useState } from "react";

interface LogTailProps {
    url: string;        // SSE-endpoint
    maxLines?: number;  // Hvor mange linjer vi holder i state
}

export function LogTail({ url, maxLines = 1000 }: LogTailProps) {
    const [lines, setLines] = useState<string[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [paused, setPaused] = useState(false);
    const eventSourceRef = useRef<EventSource | null>(null);
    const containerRef = useRef<HTMLDivElement>(null);

    // --- Setup SSE using apiSse ---
    useEffect(() => {
        const es = new EventSource(url);
        eventSourceRef.current = es;

        es.onmessage = (event) => {
            if (!paused) {
                setLines((prev) => [...prev, event.data].slice(-maxLines));
            }
        };

        es.onerror = () => {
            setError("SSE connection lost");
        };

        return () => es.close();
    }, [url, paused, maxLines]);

    // --- Auto-scroll til bunn ---
    useEffect(() => {
        if (!paused && containerRef.current) {
            containerRef.current.scrollTop = containerRef.current.scrollHeight;
        }
    }, [lines, paused]);

    // --- Håndtering av pause/resume ---
    const togglePause = () => setPaused((prev) => !prev);

    // --- Refresh log (clear + reconnect) ---
    const refreshLog = () => {
        setLines([]);
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = new EventSource(url);
        }
    };

    // --- Fargekode basert på nivå ---
    const getLineColor = (line: string) => {
        if (line.includes("ERROR")) return "error.main";
        if (line.includes("WARN")) return "warning.main";
        return "text.primary";
    };

    return (
        <Paper
            elevation={2}
            sx={{
                height: "50%",
                width: "100%",
                overflowY: "auto",
                fontFamily: "monospace",
                p: 1,
                position: "relative",
            }}
            ref={containerRef}
        >
            <Box
                sx={{
                    position: "sticky",
                    top: 0,
                    bgcolor: "background.paper",
                    zIndex: 10,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    mb: 1,
                }}
            >
                <Typography variant="subtitle2">Live Log</Typography>
                <Box>
                    <IconButton size="small" onClick={togglePause} title={paused ? "Resume" : "Pause"}>
                        {paused ? <PlayArrowIcon fontSize="small" /> : <PauseIcon fontSize="small" />}
                    </IconButton>
                    <IconButton size="small" onClick={refreshLog} title="Clear & Refresh">
                        <ReplayIcon fontSize="small" />
                    </IconButton>
                </Box>
            </Box>

            {error && <Typography color="error">{error}</Typography>}

            {lines.map((line, idx) => (
                <Typography
                    key={idx}
                    sx={{
                        whiteSpace: "pre-wrap",
                        wordBreak: "break-word",
                        color: getLineColor(line),
                    }}
                    variant="body2"
                >
                    {line}
                </Typography>
            ))}
        </Paper>
    );
}