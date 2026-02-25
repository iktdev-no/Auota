import { Button } from "@mui/material";
import type { JSX } from "react";

export function ExportStoreButton(): JSX.Element {
    const handleExport = async (): Promise<void> => {
        const res = await fetch("/encryption/export/store", {
            method: "GET"
        });

        if (!res.ok) {
            console.error("Failed to export store");
            return;
        }

        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = url;
        a.download = "AuotaStore.key";
        a.click();

        window.URL.revokeObjectURL(url);
    };

    return (
        <Button variant="contained" color="primary" onClick={handleExport}>
            Export Encryption Store
        </Button>
    );
}
