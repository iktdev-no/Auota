import InboxIcon from "@mui/icons-material/Inbox";
import { Box, Typography } from "@mui/material";

export function EmptyFolder({ text = "Mappen er tom" }: { text?: string }) {
    return (
        <Box
            sx={{
                height: "90%",
                display: "flex",
                flexDirection: "column",
                justifyContent: "center",
                alignItems: "center",
                opacity: 0.6,
                padding: 4,
                textAlign: "center"
            }}
        >
            <InboxIcon sx={{ fontSize: 48, marginBottom: 1 }} />
            <Typography variant="body1">{text}</Typography>
        </Box>
    );
}
