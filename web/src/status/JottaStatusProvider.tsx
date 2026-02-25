import { createContext, useContext, useEffect, useState, type PropsWithChildren } from "react";
import { getJottaStatus } from "../api/jottaStatus";
import { subscribe } from "../sse/eventBus";
import type { JottaSummary } from "../types/types";

type StatusContextType = {
    status: JottaSummary | null;
};

const StatusContext = createContext<StatusContextType | null>(null);

export function JottaStatusProvider({ children }: PropsWithChildren) {
    const [status, setStatus] = useState<JottaSummary | null>(null);

    async function refreshStatus() {
        try {
            const data = await getJottaStatus();
            setStatus(data);
        } catch {
            setStatus(null);
        }
    }

    // Initial fetch
    useEffect(() => {
        refreshStatus();
    }, []);

    // SSE: status updates (live)
    useEffect(() => {
        return subscribe("status.jotta", (data: JottaSummary) => {
            setStatus(data);
        });
    }, []);

    // SSE: login completed → fetch status
    useEffect(() => {
        return subscribe("jotta.loggedIn", () => {
            refreshStatus();
        });
    }, []);

    // SSE: logout completed → clear status
    useEffect(() => {
        return subscribe("jotta.loggedOut", () => {
            setStatus(null);
        });
    }, []);

    return (
        <StatusContext.Provider value={{ status }}>
            {children}
        </StatusContext.Provider>
    );
}

export function useJottaStatus() {
    const ctx = useContext(StatusContext);
    if (!ctx) throw new Error("useJottaStatus must be used inside JottaStatusProvider");
    return ctx.status;
}
