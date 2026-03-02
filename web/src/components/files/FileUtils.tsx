import DataObjectIcon from '@mui/icons-material/DataObject'
import ImageIcon from '@mui/icons-material/Image'
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile"
import MovieIcon from '@mui/icons-material/Movie'
import SubtitlesIcon from '@mui/icons-material/Subtitles'
import type { JSX } from 'react'


const videoExtensions = ["mp4", "mkv", "mov", "avi", "webm", "ts", "m2ts"];
const subtitleExtensions = ["srt", "ass", "vtt", "smi"];
const pictureExtensions = [
    "webp", "png", "jpeg", "jpg",
    "avif", "heic", "heif", "bmp", "tiff", "tif"
]


export function getFileIcon(extension: string, color: string): JSX.Element {
    const ext = extension.toLowerCase()
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
    return <InsertDriveFileIcon fontSize='large' key="main" sx={{ color: color }} />
}

export function getFileColor(extension: string): string {
    const ext = extension.toLowerCase()
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
    return "#bdbdbd";
}