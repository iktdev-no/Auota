import { Box, Tooltip } from "@mui/material";
import { useSseConnection } from "../sse/useSseConnection";

export function SseDot() {
    const connected = useSseConnection();

    return (
        <Tooltip title={connected ? "SSE tilkoblet" : "SSE frakoblet"}>
            <Box
                sx={{
                    width: 12,
                    height: 12,
                    borderRadius: "50%",
                    backgroundColor: connected ? "#4caf50" : "#f44336",
                    transition: "background-color 0.3s ease",
                }}
            />
        </Tooltip>
    );
}
