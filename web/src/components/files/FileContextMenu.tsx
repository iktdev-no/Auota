import { Menu, MenuItem } from "@mui/material";
import type { FileAction } from "../../types/types";
import type { UnifiedFile } from "../../types/webtypes";

export interface FileContextMenuProps {
    file: UnifiedFile | null;
    position: { mouseX: number; mouseY: number } | null;
    onClose: () => void;
    onFileAction: (action: FileAction, file: UnifiedFile) => void;
    onCopyPath: (file: UnifiedFile) => void;
}

export default function FileContextMenu({
    file,
    position,
    onClose,
    onFileAction,
    onCopyPath
}: FileContextMenuProps) {
    if (!file) return null;
    console.log(file)
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
            {file.actions?.map(action => (
                <MenuItem
                    key={action.id}
                    onClick={() => onFileAction(action, file)}
                >
                    {action.title}
                </MenuItem>
            ))}

            <MenuItem onClick={() => onCopyPath(file)}>Kopier sti</MenuItem>
        </Menu>
    );
}
