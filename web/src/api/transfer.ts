import type { JottaTransfer } from "../types/types";
import { apiGet, apiPostPlaintext } from "./client";

export async function uploadFileOrFolder(localPath: string) {
    return apiPostPlaintext("/transfer/upload", localPath);
}

export async function downloadFileOrFolder(path: string) {
    return apiPostPlaintext(`/transfer/download`, path);
}

export async function uploadLogs() {
    return apiGet<JottaTransfer[]>("/transfer/upload/list")
}

export async function downloadLogs() {
    return apiGet<JottaTransfer[]>("/transfer/download/list")
}