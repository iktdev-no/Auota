import { Box, MenuItem, Select, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { apiGet } from "../api/client";
import { LogTail } from "../components/logging/LogTail";

export function LogsPage() {
    const [files, setFiles] = useState<string[]>([]);
    const [selectedFile, setSelectedFile] = useState<string | null>(null);

    useEffect(() => {
        apiGet<string[]>("/logs/list").then(res => {
            setFiles(res);
            if (res.length > 0) setSelectedFile(res[0]);
        });
    }, []);

    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                height: "100%",    // fyller main-container
                p: 2,
            }}
        >
            <Typography variant="h6" gutterBottom>
                Log Viewer
            </Typography>

            <Select
                value={selectedFile ?? ""}
                onChange={(e) => setSelectedFile(e.target.value)}
                sx={{ mb: 2 }}
            >
                <MenuItem value="jotta">Jotta Log</MenuItem>
                {files.map(file => (
                    <MenuItem key={file} value={file}>{file}</MenuItem>
                ))}
            </Select>

            {selectedFile && (
                <Box
                    sx={{
                        flexGrow: 1,         // tar all tilgjengelig plass
                        display: "flex",
                        flexDirection: "column",
                        overflow: "hidden",  // vi lar LogTail håndtere sin scroll
                    }}
                >
                    <LogTail
                        url={
                            selectedFile === "jotta"
                                ? "/api/logs/jotta"
                                : `/api/logs/file?path=${encodeURIComponent(selectedFile)}`
                        }
                    />
                </Box>
            )}
        </Box>
    );
}