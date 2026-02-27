import type { EncryptionStatus } from "../types/types";
import { apiDelete, apiDownload, apiGet, apiPost, apiPostPlaintext } from "./client";

export function getEncryptionStatus() {
    return apiGet<EncryptionStatus>("/encryption/status");
}

export function toggleEncryption(enabled: boolean) {
    return apiPost<boolean, EncryptionStatus>("/encryption/encrypt", enabled);
}

export function updateEncryptionPassword(password: string) {
    return apiPostPlaintext<EncryptionStatus>("/encryption/password", password);
}


export function enableOverride() {
    return apiPost("/encryption/override/enable", {});
}

export function disableOverride() {
    return apiPost("/encryption/override/disable", {});
}

export function manualMount() {
    return apiPost("/encryption/mount/manual", {});
}

export function manualUnmount() {
    return apiPost("/encryption/unmount/manual", {});
}

export function downloadEncryptionStore() {
    return apiDownload("/encryption/export/store");
}



export function importEncryptionStore(json: string) {
    return apiPost<string, EncryptionStatus>("/encryption/import/store", json);
}

export function deleteEncryptionStore() {
    return apiDelete<EncryptionStatus>("/encryption/store");
}
