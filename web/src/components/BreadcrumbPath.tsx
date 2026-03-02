// src/components/BreadcrumbPath.tsx
import HomeIcon from "@mui/icons-material/Home";
import { Breadcrumbs, Chip } from "@mui/material";

export interface BreadcrumbPathProps {
    root: "root" | "local" | "jotta";
    path: string;
    onNavigate: (root: "root" | "local" | "jotta", path: string) => void;
    onRoot: () => void;
}

function splitPath(path: string): string[] {
    if (!path || path === "/") return [];
    return path.split("/").filter(Boolean);
}

function buildPath(parts: string[], index: number): string {
    return "/" + parts.slice(0, index + 1).join("/");
}

const BreadcrumbPath = ({ root, path, onNavigate, onRoot }: BreadcrumbPathProps) => {
    const segments = splitPath(path);

    return (
        <Breadcrumbs separator="›">
            <Chip
                icon={<HomeIcon />}
                label="Root"
                clickable
                onClick={() => onRoot()}
                sx={{ fontWeight: 600 }}
            />

            <Chip
                label={root === "local" ? "Local" : "Jottacloud"}
                clickable
                onClick={() => onNavigate(root, "/")}
                variant="outlined"
                sx={{ fontWeight: 500 }}
            />

            {segments.map((segment, index) => {
                const p = buildPath(segments, index);
                const display = decodeURIComponent(segment);
                return (
                    <Chip
                        key={p}
                        label={display}
                        clickable
                        onClick={() => onNavigate(root, p)}
                        variant="outlined"
                        sx={{ fontWeight: 500 }}
                    />
                );
            })}
        </Breadcrumbs>

    );
};

export default BreadcrumbPath;
