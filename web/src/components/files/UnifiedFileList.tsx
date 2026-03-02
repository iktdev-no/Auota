import CloudOffIcon from "@mui/icons-material/CloudOff";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import FolderIcon from "@mui/icons-material/Folder";
import ForwardIcon from "@mui/icons-material/Forward";
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile";
import RemoveModeratorIcon from "@mui/icons-material/RemoveModerator";
import ShieldIcon from "@mui/icons-material/Shield";
import {
    Box,
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText
} from "@mui/material";
import type { JSX, MouseEvent } from "react";
import type { UnifiedFile } from "../../types/webtypes";
import { normalDate } from "../../utils/utils";

export interface UnifiedFileListProps {
    files: UnifiedFile[];
    onOpenFolder: (file: UnifiedFile) => void;
    onContextMenu: (event: MouseEvent<HTMLElement>, file: UnifiedFile) => void;
}

export function UnifiedFileList(props: UnifiedFileListProps): JSX.Element {
    const { files, onOpenFolder, onContextMenu } = props;

    const getIcon = (file: UnifiedFile): JSX.Element => {
        const base =
            file.type === "Folder" ? (
                <FolderIcon key="base" fontSize="large" sx={{ color: "#fbc02d" }} />
            ) : (
                <InsertDriveFileIcon key="base" fontSize="large" sx={{ color: "#90caf9" }} />
            );

        const icons: JSX.Element[] = [base];

        if (file.isEncrypted) {
            icons.push(
                <ShieldIcon
                    key="enc"
                    fontSize="small"
                    sx={{ position: "absolute", bottom: 4, right: 0 }}
                />
            );
        }

        if (file.isInBackup) {
            icons.push(
                <CloudUploadIcon
                    key="up"
                    fontSize="small"
                    sx={{ position: "absolute", bottom: 4, right: 0 }}
                />
            );
        }

        if (file.isExcludedFromBackup) {
            icons.push(
                <CloudOffIcon
                    key="off"
                    fontSize="small"
                    sx={{ position: "absolute", bottom: 4, right: 0 }}
                />
            );
        }

        if (file.isDataSource) {
            icons.push(
                <RemoveModeratorIcon
                    key="ds"
                    fontSize="small"
                    sx={{ position: "absolute", bottom: 4, right: 0 }}
                />
            );
            icons.push(
                <ForwardIcon
                    key="ds2"
                    fontSize="small"
                    sx={{ position: "absolute", top: 8, left: 8 }}
                />
            );
        }

        return (
            <Box sx={{ position: "relative", display: "inline-block" }}>
                {icons}
            </Box>
        );
    };


    return (
        <List sx={{ bgcolor: "background.paper" }}>
            {files.map((f) => (
                <ListItemButton
                    key={f.uri}
                    onClick={() => f.type === "Folder" && onOpenFolder(f)}
                    onContextMenu={(e) => onContextMenu(e, f)}
                >
                    <ListItemIcon>{getIcon(f)}</ListItemIcon>
                    <ListItemText
                        primary={f.name}
                        secondary={normalDate.format(new Date(f.created))}
                    />
                </ListItemButton>
            ))}
        </List>
    );
}
