#!/usr/bin/env bash
set -e

IMAGE_NAME="japp-dev"
DOCKERFILE="Dockerfile.dev"
CHECKSUM_FILE=".dockerfile_dev_checksum"
FLAGS_FILE=".dev_flags"

GREEN="\033[0;32m"
RED="\033[0;31m"
RESET="\033[0m"

echo "=== Japp Dev Runner ==="

# -----------------------------
# 1. Handle flags
# -----------------------------
NO_MEMORY="false"

# Load previous flags
if [ -f "$FLAGS_FILE" ]; then
    source "$FLAGS_FILE"
fi

# Override with CLI args
for arg in "$@"; do
    case $arg in
        --no-memory)
            NO_MEMORY="true"
            ;;
        --memory)
            NO_MEMORY="false"
            ;;
    esac
done

# Save flags
echo "NO_MEMORY=$NO_MEMORY" > "$FLAGS_FILE"

# -----------------------------
# 2. Wipe or keep session
# -----------------------------
if [ "$NO_MEMORY" = "true" ]; then
    echo -e "${RED}[ACTION] WIPING Jottacloud session (japp_jottad volume)${RESET}"
    docker volume rm -f japp_jottad >/dev/null 2>&1 || true
else
    echo -e "${GREEN}[ACTION] Keeping Jottacloud session (japp_jottad volume persists)${RESET}"
fi

# -----------------------------
# 3. Build dev image if needed
# -----------------------------
if ! docker image inspect $IMAGE_NAME >/dev/null 2>&1; then
    echo "[INFO] Dev image not found. Building..."
    docker build -f $DOCKERFILE -t $IMAGE_NAME .
    sha256sum $DOCKERFILE > $CHECKSUM_FILE
else
    echo "[INFO] Dev image exists."
fi

if [ -f "$CHECKSUM_FILE" ]; then
    OLD_SUM=$(awk '{print $1}' $CHECKSUM_FILE)
    NEW_SUM=$(sha256sum $DOCKERFILE | awk '{print $1}')

    if [ "$OLD_SUM" != "$NEW_SUM" ]; then
        echo "[INFO] Dockerfile.dev changed. Rebuilding dev image..."
        docker build -f $DOCKERFILE -t $IMAGE_NAME .
        sha256sum $DOCKERFILE > $CHECKSUM_FILE
    else
        echo "[INFO] Dockerfile.dev unchanged."
    fi
else
    echo "[INFO] No checksum file found. Building dev image..."
    docker build -f $DOCKERFILE -t $IMAGE_NAME .
    sha256sum $DOCKERFILE > $CHECKSUM_FILE
fi

# -----------------------------
# 4. Build app.jar
# -----------------------------
echo "[INFO] Building app.jar with Gradle..."
./gradlew build

APP_JAR="./build/libs/app.jar"

if [ ! -f "$APP_JAR" ]; then
    echo "[ERROR] app.jar not found at $APP_JAR"
    exit 1
fi

echo "[INFO] app.jar built successfully."

# -----------------------------
# 5. Run container
# -----------------------------
echo "[INFO] Starting dev container..."

docker run --rm -it \
    --cap-add SYS_ADMIN \
    --device /dev/fuse \
    --security-opt apparmor=unconfined \
    -v "$(pwd)/build/libs/app.jar":/usr/share/app/app.jar \
    -v japp_config:/config \
    -v japp_crypt_backend:/crypt-backend \
    -v japp_crypt:/crypt \
    -v japp_jottad:/root/.jottad \
    -p 8080:8080 \
    $IMAGE_NAME
