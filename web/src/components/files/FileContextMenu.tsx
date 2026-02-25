import { Menu, MenuItem } from "@mui/material";
import type { FileAction, IFile } from "../../types/types";

export interface FileContextMenuProps {
    file: IFile | null
    position: { mouseX: number; mouseY: number } | null
    onClose: () => void
    onFileAction: (action: FileAction, file: IFile) => void
    onCopyPath: (file: IFile) => void
}

export function FileContextMenu({
    file,
    position,
    onClose,
    onFileAction,
    onCopyPath
}: FileContextMenuProps) {
    if (!file) return null

    return (
        <Menu
            open={!!position}
            onClose={onClose}
            anchorReference="anchorPosition"
            anchorPosition={
                position
                    ? { top: position.mouseY, left: position.mouseX }
                    : undefined
            }
        >

            {/* File actions */}
            {file.actions.map(action => (
                <MenuItem
                    key={action.id}
                    onClick={() => onFileAction(action, file)}
                >
                    {action.title}
                </MenuItem>
            ))}

            {/* Always available */}
            <MenuItem onClick={() => onCopyPath(file)}>Kopier sti</MenuItem>
        </Menu>
    )
}
