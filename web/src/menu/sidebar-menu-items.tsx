import ArticleIcon from "@mui/icons-material/Article";
import DashboardIcon from "@mui/icons-material/Dashboard";
import FolderIcon from "@mui/icons-material/Folder";
import SettingsIcon from "@mui/icons-material/Settings";
import { useNavigate } from "react-router-dom";

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
