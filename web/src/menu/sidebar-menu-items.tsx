import type { JSX } from "@emotion/react/jsx-runtime";
import ArticleIcon from "@mui/icons-material/Article";
import CloudIcon from '@mui/icons-material/Cloud';
import DashboardIcon from "@mui/icons-material/Dashboard";
import FolderIcon from "@mui/icons-material/Folder";
import SettingsIcon from "@mui/icons-material/Settings";
import { Box } from "@mui/material";
import { useNavigate } from "react-router-dom";

function CloudFolder(): JSX.Element {
    return (
        <Box
            sx={{
                display: "inline-flex",
                alignItems: "center",   // sentrerer vertikalt med andre inline ikoner
                justifyContent: "center",
                position: "relative",
                width: 24,              // standard MUI icon-størrelse
                height: 24,
            }}
        >
            <FolderIcon
                fontSize="medium"
                sx={{ color: "#a654fc" }}
            />
            <CloudIcon
                sx={{
                    fontSize: 12,
                    position: "absolute",
                    top: 7,
                    color: "white",
                }}
            />
        </Box>
    );
}


export function useSidebarMenu() {
    const navigate = useNavigate();

    return {
        topMenu: [
            {
                id: "status",
                label: "Status",
                icon: <DashboardIcon />,
                onClick: () => navigate("/")
            },
            {
                id: "logs",
                label: "Logg",
                icon: <ArticleIcon />,
                onClick: () => navigate("/logs")
            },
            /* {
                id: "backup",
                label: "Backup",
                icon: <CloudUploadIcon />,
                onClick: () => navigate("/backup")
            },  */
            {
                id: "files",
                label: "Filer",
                icon: <FolderIcon />,
                onClick: () => navigate("/files")
            },
            {
                id: "jfiles",
                label: "Cloud files",
                icon: <CloudFolder />,
                onClick: () => navigate("/jfiles")
            }
        ],
        bottomMenu: [
            /*    {
                    id: "health",
                    label: "Helse",
                    icon: <MonitorHeartIcon />,
                    onClick: () => navigate("/health")
                }, */
            {
                id: "settings",
                label: "Innstillinger",
                icon: <SettingsIcon />,
                onClick: () => navigate("/settings")
            }
        ]
    };
}
