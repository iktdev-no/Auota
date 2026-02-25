import { useEffect, useState } from "react";
import { getSystemHealth } from "../api/health";
import type { SystemHealth } from "../types/types";

export function useSystemHealth() {
    const [health, setHealth] = useState<SystemHealth | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let mounted = true;

        const fetchHealth = async () => {
            try {
                const data = await getSystemHealth();
                if (mounted) setHealth(data);
            } finally {
                if (mounted) setLoading(false);
            }
        };

        fetchHealth();

        // Poll hvert 5. sekund (kan byttes til SSE senere)
        const id = setInterval(fetchHealth, 5000);

        return () => {
            mounted = false;
            clearInterval(id);
        };
    }, []);

    return { health, loading };
}
