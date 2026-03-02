// src/utils/localAdapter.ts
import type { IFile, JottaFs } from "../types/types";
import type { UnifiedFile } from "../types/webtypes";


export function mapLocalToUnified(file: IFile): UnifiedFile {
    return {
        type: file.type,
        name: file.name,
        uri: file.uri,
        created: file.created,
        extension: (file.type === "File") ? file.extension : "",
        size: (file.type === "File") ? file.size : -1,

        isEncrypted: file.isEncrypted ?? false,
        isInBackup: file.isInBackup ?? false,
        isExcludedFromBackup: file.isExcludedFromBackup ?? false,
        isDataSource: file.isDataSource ?? false,

        actions: file.actions
    };
}

// src/utils/jottaAdapter.ts

export function mapJottaToUnified(jfs: JottaFs): UnifiedFile[] {
    const folders: UnifiedFile[] = (jfs.Folders ?? []).map((f) => ({
        type: "Folder",
        name: f.Name,
        uri: f.Path ?? "",
        created: 0,
        extension: "",
        size: -1,

        isEncrypted: false,
        isInBackup: false,
        isExcludedFromBackup: false,
        isDataSource: false,

        actions: f.actions
    }));

    const files: UnifiedFile[] = (jfs.Files ?? []).map((f) => ({
        type: "File",
        name: f.Name,
        uri: f.Path,
        created: 0,
        extension: f.extension ?? "",
        size: f.Size,

        isEncrypted: false,
        isInBackup: false,
        isExcludedFromBackup: false,
        isDataSource: false,

        actions: f.actions
    }));

    return [...folders, ...files];
}
