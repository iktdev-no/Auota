#!/usr/bin/env bash
set -e

# -----------------------------
# 0. Paths and config
# -----------------------------
DEV_DIR=".dev"
mkdir -p "$DEV_DIR"

IMAGE_NAME="auota-dev"
DOCKERFILE="docker/Dev.dockerfile"
DOCKERFILE_CHECKSUM="$DEV_DIR/dockerfile_dev_checksum"
SRC_CHECKSUM="$DEV_DIR/src_checksum"
WEB_CHECKSUM="$DEV_DIR/web_checksum"
FLAGS_FILE="$DEV_DIR/dev_flags"

GREEN="\033[0;32m"
RED="\033[0;31m"
RESET="\033[0m"

echo "=== Auota Dev Runner ==="

# -----------------------------
# 1. Handle flags
# -----------------------------
NO_MEMORY="false"
FULL_BUILD="false"

if [ -f "$FLAGS_FILE" ]; then
    source "$FLAGS_FILE"
fi

for arg in "$@"; do
    case $arg in
        --no-memory) NO_MEMORY="true" ;;
        --memory) NO_MEMORY="false" ;;
        --full) FULL_BUILD="true" ;;
    esac
done

echo "NO_MEMORY=$NO_MEMORY" > "$FLAGS_FILE"
echo "FULL_BUILD=$FULL_BUILD" >> "$FLAGS_FILE"

# -----------------------------
# 2. Wipe or keep session
# -----------------------------
if [ "$NO_MEMORY" = "true" ]; then
    echo -e "${RED}[ACTION] WIPING Jottacloud session (Auota_jottad volume)${RESET}"
    docker volume rm -f Auotajottad >/dev/null 2>&1 || true
else
    echo -e "${GREEN}[ACTION] Keeping Jottacloud session (Auota_jottad volume persists)${RESET}"
fi

# -----------------------------
# 3. Build dev image if needed
# -----------------------------
build_image() {
    docker build -f "$DOCKERFILE" -t "$IMAGE_NAME" .
    sha256sum "$DOCKERFILE" > "$DOCKERFILE_CHECKSUM"
}

if ! docker image inspect "$IMAGE_NAME" >/dev/null 2>&1; then
    echo "[INFO] Dev image not found. Building..."
    build_image
else
    if [ -f "$DOCKERFILE_CHECKSUM" ]; then
        OLD_SUM=$(awk '{print $1}' "$DOCKERFILE_CHECKSUM")
        NEW_SUM=$(sha256sum "$DOCKERFILE" | awk '{print $1}')
        if [ "$OLD_SUM" != "$NEW_SUM" ]; then
            echo "[INFO] Dockerfile changed. Rebuilding dev image..."
            build_image
        else
            echo "[INFO] Dockerfile unchanged."
        fi
    else
        echo "[INFO] No Dockerfile checksum found. Building dev image..."
        build_image
    fi
fi

# -----------------------------
# 4. Optional full build (frontend -> static -> Gradle)
# -----------------------------
FRONTEND_DIR="./web"
STATIC_DIR="./src/main/resources/static"

compute_checksum() {
    local dir=$1
    find "$dir" -type f -exec sha256sum {} + | sort | sha256sum | awk '{print $1}'
}

if [ "$FULL_BUILD" = "true" ]; then
    # Sjekk web checksum
    NEW_WEB_SUM=$(compute_checksum "$FRONTEND_DIR")
    OLD_WEB_SUM=""
    if [ -f "$WEB_CHECKSUM" ]; then OLD_WEB_SUM=$(cat "$WEB_CHECKSUM"); fi

    # Sjekk src checksum
    NEW_SRC_SUM=$(compute_checksum "./src")
    OLD_SRC_SUM=""
    if [ -f "$SRC_CHECKSUM" ]; then OLD_SRC_SUM=$(cat "$SRC_CHECKSUM"); fi

    # Hvis web har endret seg -> bygg frontend
    if [ "$NEW_WEB_SUM" != "$OLD_WEB_SUM" ]; then
        echo "[INFO] Frontend changed. Building frontend..."
        pushd "$FRONTEND_DIR" >/dev/null
        npm install
        npm run build
        popd >/dev/null

        echo "[INFO] Copying frontend build to $STATIC_DIR..."
        rm -rf "$STATIC_DIR"
        mkdir -p "$STATIC_DIR"
        cp -r "$FRONTEND_DIR/dist/." "$STATIC_DIR"

        echo "$NEW_WEB_SUM" > "$WEB_CHECKSUM"
    else
        echo "[INFO] Frontend unchanged. Skipping frontend build."
    fi

    # Hvis src har endret seg -> bygg backend (Gradle)
    if [ "$NEW_SRC_SUM" != "$OLD_SRC_SUM" ]; then
        echo "[INFO] Backend source changed. Running Gradle build..."
        ./gradlew build
        echo "$NEW_SRC_SUM" > "$SRC_CHECKSUM"
    else
        echo "[INFO] Backend source unchanged. Skipping Gradle build."
    fi
else
    # Standard build uten checksum-sjekk
    echo "[INFO] Full build flag not set. Building Gradle project..."
    ./gradlew build
fi

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
    --name auota-dev \
    --cap-add SYS_ADMIN \
    --device /dev/fuse \
    --security-opt apparmor=unconfined \
    -v "$(pwd)/build/libs/app.jar":/usr/share/app/app.jar \
    -v ./docker/entrypoints/:/docker-entrypoint.d/ \
    -v auota_config:/config \
    -v auota_crypt_backend:/dataEncrypted \
    -v auota_jottad:/root/.jottad \
    -p 8080:8080 \
    $IMAGE_NAME