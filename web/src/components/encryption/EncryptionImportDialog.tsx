import {
    Box,
    Button,
    Dialog,
    DialogContent,
    DialogTitle
} from "@mui/material";
import { useState, type ChangeEvent, type DragEvent, type JSX } from "react";

export function EncryptionImportDialog(): JSX.Element {
    const [open, setOpen] = useState<boolean>(false);
    const [file, setFile] = useState<File | null>(null);

    const handleDrop = (e: DragEvent<HTMLDivElement>): void => {
        e.preventDefault();
        const droppedFile = e.dataTransfer.files?.[0] ?? null;
        setFile(droppedFile);
    };

    const handleFileSelect = (e: ChangeEvent<HTMLInputElement>): void => {
        const selected = e.target.files?.[0] ?? null;
        setFile(selected);
    };

    const handleUpload = async (): Promise<void> => {
        if (!file) return;

        const text = await file.text();

        const res = await fetch("/encryption/import/store", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: text
        });

        const status = await res.json();
        console.log("Import result:", status);

        setOpen(false);
    };

    return (
        <>
            <Button variant="outlined" onClick={() => setOpen(true)}>
                Import Encryption Store
            </Button>

            <Dialog open={open} onClose={() => setOpen(false)}>
                <DialogTitle>Import Encryption Store</DialogTitle>

                <DialogContent>
                    <Box
                        sx={{
                            border: "2px dashed #aaa",
                            padding: 4,
                            textAlign: "center",
                            borderRadius: 2,
                            cursor: "pointer"
                        }}
                        onDragOver={(e: DragEvent<HTMLDivElement>) => e.preventDefault()}
                        onDrop={handleDrop}
                    >
                        {file ? (
                            <strong>{file.name}</strong>
                        ) : (
                            "Drop AuotaStore.key here or click to select"
                        )}

                        <input
                            type="file"
                            accept=".key"
                            style={{ display: "none" }}
                            id="import-store-file"
                            onChange={handleFileSelect}
                        />
                    </Box>

                    <Button
                        variant="contained"
                        color="primary"
                        fullWidth
                        sx={{ mt: 2 }}
                        onClick={handleUpload}
                        disabled={!file}
                    >
                        Import
                    </Button>
                </DialogContent>
            </Dialog>
        </>
    );
}
