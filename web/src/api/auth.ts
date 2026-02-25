import type { AuthResponse } from "../types/types";
import { apiGet, apiPost } from "./client";

export function startLogin() {
    return apiPost<null, AuthResponse>("/auth/login/start", null);
}

export function answerLogin(sessionId: string, input: string) {
    return apiPost<{ sessionId: string; input: string }, AuthResponse>(
        "/auth/login/answer",
        { sessionId, input }
    );
}

export function startLogout() {
    return apiPost<null, AuthResponse>("/auth/logout/start", null);
}

export function answerLogout(sessionId: string, input: string) {
    return apiPost<{ sessionId: string; input: string }, AuthResponse>(
        "/auth/logout/answer",
        { sessionId, input }
    );
}

export function poll(sessionId: string) {
    return apiGet<AuthResponse>(`/auth/poll?sessionId=${sessionId}`);
}
