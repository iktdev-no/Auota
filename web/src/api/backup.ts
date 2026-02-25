import type { BackupIEUpdate, BackupItem, EncryptionStatus } from "../types/types";
import { apiDeletePlaintext, apiGet, apiPost, apiPostPlaintext } from "./client";

export function getBackupFolders() {
    return apiGet<BackupItem[]>("/backup/list");
}

export function addBackup(path: string) {
    return apiPostPlaintext<string>(
        "/backup/add",
        path
    )
}

export function removeBackup(path: string) {
    return apiDeletePlaintext<string>("/backup/remove", path)
}

export function ignoreBackupOrFolder(data: BackupIEUpdate) {
    return apiPost<BackupIEUpdate, EncryptionStatus>(
        "/backup/exclude", data
    )
}

export function unignoreBackupOrFolder(data: BackupIEUpdate) {
    return apiPost<BackupIEUpdate, EncryptionStatus>(
        "/backup/include", data
    )
}