import { type PropsWithChildren, useEffect } from "react";
import { apiSse } from "../api/client";
import { dispatch } from "./eventBus";

export function SseProvider({ children }: PropsWithChildren) {
    useEffect(() => {
        let es: EventSource | null = null;
        let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
        const reconnectDelay = 3000;

        const connect = () => {
            if (es) es.close();

            es = apiSse(
                (eventName, data) => {
                    // dispatch ALWAYS works because eventBus is global
                    dispatch(eventName, data);
                },
                () => {
                    // reconnect on error
                    if (!reconnectTimer) {
                        reconnectTimer = setTimeout(() => {
                            reconnectTimer = null;
                            connect();
                        }, reconnectDelay);
                    }
                }
            );
        };

        connect();

        return () => {
            if (es) es.close();
            if (reconnectTimer) clearTimeout(reconnectTimer);
        };
    }, []);

    return <>{children}</>;
}

