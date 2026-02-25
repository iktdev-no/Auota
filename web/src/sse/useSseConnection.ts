import { useEffect, useState } from "react";
import { subscribe } from "./eventBus";

export function useSseConnection() {
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        return subscribe("sse-connection", ({ connected }) => {
            setConnected(connected);
        });
    }, []);

    return connected;
}
