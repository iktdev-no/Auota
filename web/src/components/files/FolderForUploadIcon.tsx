import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import Folder from '@mui/icons-material/Folder';
import { Box } from "@mui/material";

export function FolderForUploadIcon() {
    return (
        <Box sx={{ position: "relative", display: "inline-block" }}>
            <Folder fontSize='large' sx={{
                color: "#7919d2"
            }} />
            <CloudUploadIcon
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
