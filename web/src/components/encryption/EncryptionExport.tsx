import { Button } from "@mui/material";
import { downloadEncryptionStore } from "../../api/encryption";

export function ExportStoreButton() {
    const handleExport = async () => {
        try {
            const blob = await downloadEncryptionStore();
            const url = window.URL.createObjectURL(blob);

            const a = document.createElement("a");
            a.href = url;
            a.download = "AuotaStore.key";
            a.click();

            window.URL.revokeObjectURL(url);
        } catch (err) {
            console.error("Export failed", err);
        }
    };

    return (
        <Button variant="contained" onClick={handleExport}>
            Export Encryption Store
        </Button>
    );
}
