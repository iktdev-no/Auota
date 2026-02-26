import type { EncryptionStatus, JottadStatus, JottaSummary } from "../types/types";
import { apiGet } from "./client";

export async function getJottaStatus(): Promise<JottaSummary> {
    return apiGet<JottaSummary>("/status/jotta");
}

export async function getEncryptionStatus(): Promise<EncryptionStatus> {
    return apiGet<EncryptionStatus>("/status/encryption");
}

export async function getJottaDaemonStatus(): Promise<JottadStatus> {
    return apiGet<JottadStatus>("/status/daemon");
}
