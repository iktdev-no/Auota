import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import Folder from '@mui/icons-material/Folder';
import { Box } from "@mui/material";

export function UnencryptedFolderInputIcon() {
    return (
        <Box sx={{ position: "relative", display: "inline-block" }}>
            <Folder fontSize='large' sx={{
                color: "#ac0f0f"
            }} />
            <ArrowForwardIcon
                fontSize='medium'
                sx={{
                    position: "absolute",
                    top: 6,
                    bottom: 4,
                    left: 4,
                    right: 4,
                    color: "default.contrastText",
                }}
            />
        </Box>
    );
}
