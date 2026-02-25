import { createContext, useContext, useEffect, useState, type PropsWithChildren } from "react";
import { getEncryptionStatus } from "../api/jottaStatus";
import { subscribe } from "../sse/eventBus";
import type { EncryptionStatus } from "../types/types";

type StatusContextType = {
    status: EncryptionStatus | null;
};

const EncryptionStatusContext = createContext<StatusContextType | null>(null);

export function EncryptionStatusProvider({ children }: PropsWithChildren) {
    const [status, setStatus] = useState<EncryptionStatus | null>(null);

    async function refreshStatus() {
        try {
            const data = await getEncryptionStatus();
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
        return subscribe("status.encryption", (data: EncryptionStatus) => {
            setStatus(data);
        });
    }, []);


    return (
        <EncryptionStatusContext.Provider value={{ status }}>
            {children}
        </EncryptionStatusContext.Provider>
    );
}

export function useEncryptionStatus() {
    const ctx = useContext(EncryptionStatusContext);
    if (!ctx) throw new Error("useEncryptionStatus must be used inside EncryptionStatusProvider");
    return ctx.status;
}
