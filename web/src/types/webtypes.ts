import type { JSX } from "react"
import type { FileAction } from "./types"

export type MenuItem = {
    id: string
    label: string
    icon: JSX.Element
    onClick: () => void
}

export type MenuLayout = {
    topItems: MenuItem[]
    bottomItems: MenuItem[]
}


export interface WebEncryptionForm {
    enabled: boolean;
    password: string;
}


export interface UnifiedFile {
    type: "File" | "Folder";
    name: string;
    uri: string;
    created: number;
    extension: string;
    size: number;

    // optional metadata
    isEncrypted: boolean;
    isInBackup: boolean;
    isExcludedFromBackup: boolean;
    isDataSource: boolean;

    // actions
    actions: FileAction[];
}
