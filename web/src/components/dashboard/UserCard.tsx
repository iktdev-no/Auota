import AllInclusiveIcon from "@mui/icons-material/AllInclusive";
import { Avatar, Box, Card, CardContent, LinearProgress, Typography } from "@mui/material";
import type { JottaStatus } from "../../types/types";
import { formatBytes } from "../../utils/utils";

interface Props {
    data: JottaStatus | null;
}

export function UserCard({ data }: Props) {
    const user = data?.User;
    const acc = user?.AccountInfo;

    const usage = acc?.Usage ?? 0;
    const cap = acc?.Capacity ?? 0;
    const percent = cap > 0 ? (usage / cap) * 100 : 0;

    return (
        <Card>
            <CardContent>
                <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
                    <Avatar
                        sx={{
                            bgcolor: user?.Avatar?.Background
                                ? `rgb(${user.Avatar.Background.r}, ${user.Avatar.Background.g}, ${user.Avatar.Background.b})`
                                : "primary.main",
                            width: 56,
                            height: 56,
                            fontSize: "1.4rem"
                        }}
                    >
                        {user?.Avatar?.Initials ?? "?"}
                    </Avatar>

                    <Box>
                        <Typography variant="h6">{user?.Fullname ?? "Ukjent bruker"}</Typography>
                        <Typography variant="body2" color="text.secondary">
                            {user?.Email}
                        </Typography>
                    </Box>
                </Box>

                <Box sx={{ mt: 2 }}>
                    <Typography variant="body2">Lagringsbruk</Typography>
                    <LinearProgress
                        variant="determinate"
                        value={percent}
                        sx={{ height: 8, borderRadius: 1, mt: 1 }}
                    />
                    <Typography variant="caption">
                        {formatBytes(usage)} / {(cap > -1) ? (formatBytes(cap)) :
                            <AllInclusiveIcon
                                sx={{
                                    fontSize: "1.4rem",
                                    verticalAlign: "middle",
                                }}
                            />
                        }
                    </Typography>
                </Box>
            </CardContent>
        </Card>
    );
}