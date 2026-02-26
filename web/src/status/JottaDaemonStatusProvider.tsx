import { createContext, useContext, useEffect, useState, type PropsWithChildren } from "react";
import { getJottaDaemonStatus } from "../api/jottaStatus";
import { subscribe } from "../sse/eventBus";
import type { JottadStatus } from "../types/types";

type StatusContextType = {
    status: JottadStatus | null;
};

const JottaDaemonStatusContext = createContext<StatusContextType | null>(null);

export function JottaDaemonStatusProvider({ children }: PropsWithChildren) {
    const [status, setStatus] = useState<JottadStatus | null>(null);

    async function refreshStatus() {
        try {
            const data = await getJottaDaemonStatus();
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
        return subscribe("status.jottad", (data: JottadStatus) => {
            setStatus(data);
        });
    }, []);


    return (
        <JottaDaemonStatusContext.Provider value={{ status }}>
            {children}
        </JottaDaemonStatusContext.Provider>
    );
}

export function useJottaDaemonStatus() {
    const ctx = useContext(JottaDaemonStatusContext);
    if (!ctx) throw new Error("useJottaDaemonStatus must be used inside JottaDaemonStatusProvider");
    return ctx.status;
}
