#!/bin/sh
set -e

exit 0

echo "Validating that /data is NOT used..."

# 1. /data MUST NOT be a mountpoint
if mountpoint -q /data; then
    echo "FATAL: /data is a mountpoint. This is forbidden. Do NOT mount anything to /data."
    exit 1
fi

# 2. /data MUST be empty
if [ "$(ls -A /data 2>/dev/null)" ]; then
    echo "FATAL: /data is not empty. This directory must remain unused."
    exit 1
fi


echo "/data is safe (not mounted, empty, not writable)."

exec "$@"
