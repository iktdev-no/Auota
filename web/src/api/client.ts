import { toast } from "react-toastify";
import type { SseEnvelope } from "../types/types";

export async function apiGet<T>(
    path: string,
    opts?: {
        onError?: (status: number, body: any) => void
    }
): Promise<T> {
    const res = await fetch(`/api${path}`, {
        headers: {
            Accept: "application/json"
        }
    })

    if (!res.ok) {
        const status = res.status
        let body: any = null

        try {
            body = await res.json()
        } catch {
            body = await res.text().catch(() => null)
        }

        // Custom handler?
        if (opts?.onError) {
            opts.onError(status, body)
            return Promise.reject({ status, body })
        }

        // ⭐ Automatic toast
        const message =
            typeof body === "object" && body?.message
                ? body.message
                : `GET ${path} failed with ${status}`

        toast.error(message)

        const error: any = new Error(message)
        error.status = status
        error.body = body
        throw error
    }

    return res.json()
}

export async function apiPost<TRequest, TResponse>(
    path: string,
    body: TRequest
): Promise<TResponse> {
    return sharedApiPostBase<TResponse>(
        path,
        JSON.stringify(body),
        "application/json"
    )
}

export async function apiPostPlaintext<TResponse>(
    path: string,
    body: string
): Promise<TResponse> {
    return sharedApiPostBase<TResponse>(
        path,
        body,
        "text/plain"
    )
}



async function sharedApiPostBase<TResponse>(
    path: string,
    body: string,
    contentType: string
): Promise<TResponse> {
    const res = await fetch(`/api${path}`, {
        method: "POST",
        headers: {
            "Content-Type": contentType,
            "Accept": "*/*"
        },
        body
    })

    if (!res.ok) {
        let errorBody: any = null

        try {
            errorBody = await res.json()
        } catch {
            errorBody = await res.text().catch(() => null)
        }

        const message =
            typeof errorBody === "object" && errorBody?.message
                ? errorBody.message
                : `POST ${path} failed with ${res.status}`

        const error: any = new Error(message)
        error.status = res.status
        error.body = errorBody
        throw error
    }

    const contentTypeHeader = res.headers.get("content-type") ?? ""

    if (contentTypeHeader.includes("application/json")) {
        return res.json()
    }

    const text = await res.text()
    return text as unknown as TResponse
}

export async function apiDeletePlaintext<TResponse>(
    path: string,
    body: string
): Promise<TResponse> {
    return sharedApiDeleteBase<TResponse>(
        path,
        body,
        "text/plain"
    );
}


export async function apiDeleteJson<TRequest, TResponse>(
    path: string,
    body: TRequest
): Promise<TResponse> {
    return sharedApiDeleteBase<TResponse>(
        path,
        JSON.stringify(body),
        "application/json"
    );
}


export async function apiDelete<TResponse>(
    path: string,
): Promise<TResponse> {
    return sharedApiDeleteBase<TResponse>(
        path,
        undefined,
        "application/json"
    );
}

async function sharedApiDeleteBase<TResponse>(
    path: string,
    body: string | undefined,
    contentType: string | undefined
): Promise<TResponse> {
    const res = await fetch(`/api${path}`, {
        method: "DELETE",
        headers: {
            "Accept": "*/*",
            ...(contentType ? { "Content-Type": contentType } : {})
        },
        body
    });

    if (!res.ok) {
        let errorBody: any = null;

        try {
            errorBody = await res.json();
        } catch {
            errorBody = await res.text().catch(() => null);
        }

        const message =
            typeof errorBody === "object" && errorBody?.message
                ? errorBody.message
                : `DELETE ${path} failed with ${res.status}`;

        const error: any = new Error(message);
        error.status = res.status;
        error.body = errorBody;
        throw error;
    }

    const contentTypeHeader = res.headers.get("content-type") ?? "";

    if (contentTypeHeader.includes("application/json")) {
        return res.json();
    }

    const text = await res.text();
    return text as unknown as TResponse;
}


let errorToastShown = false;

export function apiSse(
    onEvent: (eventName: string, data: any) => void,
    onError?: () => void
): EventSource {
    const es = new EventSource("/api/events");

    es.onopen = () => {
        errorToastShown = false; // ← reset når vi kobler til igjen
        onEvent("sse-connection", { connected: true });
    };

    es.onmessage = (event) => {
        const msg: SseEnvelope = JSON.parse(event.data);
        onEvent(msg.type, msg.payload);
    };

    es.onerror = () => {
        onEvent("sse-connection", { connected: false });

        if (!errorToastShown) {
            toast.error("SSE connection lost");
            errorToastShown = true; // ← vis kun én gang
        }

        if (onError) onError();
    };

    return es;
}
