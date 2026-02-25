import type { EncryptionStatus, JottaSummary } from "../types/types";
import { apiGet } from "./client";

export async function getJottaStatus(): Promise<JottaSummary> {
    return apiGet<JottaSummary>("/status/jotta");
}

export async function getEncryptionStatus(): Promise<EncryptionStatus> {
    return apiGet<EncryptionStatus>("/status/encryption");
}
