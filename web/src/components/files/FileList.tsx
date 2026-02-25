import CloudOffIcon from '@mui/icons-material/CloudOff'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import DataObjectIcon from '@mui/icons-material/DataObject'
import { default as Folder } from "@mui/icons-material/Folder"
import ForwardIcon from '@mui/icons-material/Forward'
import ImageIcon from '@mui/icons-material/Image'
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile"
import MovieIcon from '@mui/icons-material/Movie'
import RemoveModeratorIcon from '@mui/icons-material/RemoveModerator'
import ShieldIcon from '@mui/icons-material/Shield'
import SubtitlesIcon from '@mui/icons-material/Subtitles'
import { Box, List, ListItemButton, ListItemIcon, ListItemText } from "@mui/material"
import type { JSX, PropsWithChildren } from 'react'
import type { IFile } from '../../types/types'
import { normalDate } from '../../utils'


export interface FileListProps {
    files: IFile[]
    onOpenFolder: (file: IFile) => void
    onContextMenu: (event: React.MouseEvent<HTMLElement>, file: IFile) => void
}

export function FileList({ files, onOpenFolder, onContextMenu }: FileListProps) {
    const videoExtensions = ["mp4", "mkv", "mov", "avi", "webm", "ts", "m2ts"];
    const subtitleExtensions = ["srt", "ass", "vtt", "smi"];
    const pictureExtensions = [
        "webp", "png", "jpeg", "jpg",
        "avif", "heic", "heif", "bmp", "tiff", "tif"
    ]

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
            if (videoExtensions.includes(ext)) {
                return "#42a5f5";
            }
            if (subtitleExtensions.includes(ext)) {
                return "#66bb6a"
            }

            if (pictureExtensions.includes(ext)) {
                return "#26a69a"
            }

            if (ext === "json") {
                return "#ab47bc"
            }
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
            if (videoExtensions.includes(ext)) {
                return <MovieIcon fontSize='large' key="main" sx={{ color: color }} />
            }

            if (subtitleExtensions.includes(ext)) {
                return <SubtitlesIcon fontSize='large' key="main" sx={{ color: color }} />
            }

            if (pictureExtensions.includes(ext)) {
                return <ImageIcon fontSize='large' key="main" sx={{ color: color }} />
            }

            if (ext === "json") {
                return <DataObjectIcon fontSize='large' key="main" sx={{ color: color }} />
            }
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
