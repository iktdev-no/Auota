import CloudOffIcon from '@mui/icons-material/CloudOff'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import { default as Folder } from "@mui/icons-material/Folder"
import ForwardIcon from '@mui/icons-material/Forward'
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile"
import RemoveModeratorIcon from '@mui/icons-material/RemoveModerator'
import ShieldIcon from '@mui/icons-material/Shield'
import { Box, List, ListItemButton, ListItemIcon, ListItemText } from "@mui/material"
import type { JSX, PropsWithChildren } from 'react'
import type { IFile } from '../../types/types'
import { normalDate } from '../../utils'
import { getFileColor, getFileIcon } from './FileUtils'


export interface FileListProps {
    files: IFile[]
    onOpenFolder: (file: IFile) => void
    onContextMenu: (event: React.MouseEvent<HTMLElement>, file: IFile) => void
}

export function FileList({ files, onOpenFolder, onContextMenu }: FileListProps) {

    const getMainIconColor = (file: IFile): string => {
        if (file.type === "Folder") {
            if (file.isDataSource) {
                return '#cc0000'
            } else if (file.isEncrypted) {
                return "#1976d2";
            } else if (file.isInBackup) {
                return "#7919d2";
            }
            return "#fbc02d";
        } else if (file.type === "File") {
            const ext = file.extension.toLowerCase()
            return getFileColor(ext)
        }
        return "#bdbdbd";
    }

    const getBaseIcon = (file: IFile) => {
        const color = getMainIconColor(file);
        if (file.type === "Folder") {
            return <Folder fontSize='large' key="main" sx={{
                color: color
            }} />
        } else if (file.type === 'File') {
            const ext = file.extension.toLowerCase()
            return getFileIcon(ext, color)
        }
        return <InsertDriveFileIcon fontSize='large' key="main" sx={{ color: color }} />
    }

    const getItemIcon = (file: IFile) => {
        const baseIcon = getBaseIcon(file);
        const icons: JSX.Element[] = [baseIcon]

        if (file.isEncrypted) {
            icons.push(<ShieldIcon
                key="encrypt"
                fontSize='small'
                sx={{
                    position: "absolute",
                    bottom: 4,
                    right: 0,
                    color: "default.contrastText",
                }}
            />)
        } else if (file.isInBackup) {
            icons.push(<CloudUploadIcon
                key="upload"

                fontSize='small'
                sx={{

                    position: "absolute",
                    bottom: 4,
                    right: 0,
                    color: "default.contrastText",
                }}
            />)
        } else if (file.isExcludedFromBackup) {
            icons.push(<CloudOffIcon
                key="uploadExcluded"

                fontSize='small'
                sx={{

                    position: "absolute",
                    bottom: 4,
                    right: 0,
                    color: "default.contrastText",
                }}
            />)
        }

        if (file.isDataSource) {
            icons.push(<RemoveModeratorIcon
                key="unencrypted"
                fontSize='small'
                sx={{
                    position: "absolute",
                    bottom: 4,
                    right: 0,
                    color: "default.contrastText",
                }} />)
            icons.push(<ForwardIcon fontSize='small'
                key="dataSource"
                sx={{
                    position: "absolute",
                    top: 8,
                    left: 8,
                    right: 0,
                    color: "default.contrastText",
                }}
            />)
        }


        return IconWithBadge({ children: icons })
    }




    return (
        <List sx={{ bgcolor: "background.paper" }}>
            {files.map((f) => (
                <ListItemButton
                    key={f.uri}
                    onClick={() => f.type === "Folder" && onOpenFolder(f)}
                    onContextMenu={(e) => onContextMenu(e, f)}
                >
                    <ListItemIcon>
                        {getItemIcon(f)}
                    </ListItemIcon>
                    <ListItemText
                        primary={f.name}
                        secondary={normalDate.format(new Date(f.created))}
                    />
                </ListItemButton>
            ))}
        </List>
    )
}

function IconWithBadge({ children }: PropsWithChildren) {
    return (
        <Box sx={{ position: "relative", display: "inline-block" }}>
            {children}
        </Box>
    );
}
