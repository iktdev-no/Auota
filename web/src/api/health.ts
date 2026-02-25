import type { SystemHealth } from "../types/types";
import { apiGet } from "./client";


export async function getSystemHealth(): Promise<SystemHealth> {
    const res = await apiGet<SystemHealth>("/health");
    return res;
}
