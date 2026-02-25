import Folder from '@mui/icons-material/Folder';
import ShieldIcon from '@mui/icons-material/Shield';
import { Box } from "@mui/material";

export function EncryptedFolderIcon() {
    return (
        <Box sx={{ position: "relative", display: "inline-block" }}>
            <Folder fontSize='large' sx={{
                color: "#1976d2"
            }} />
            <ShieldIcon
                fontSize='small'
                sx={{
                    position: "absolute",
                    bottom: 4,
                    right: 0,
                    color: "default.contrastText",
                }}
            />
        </Box>
    );
}
