#!/usr/bin/env bash
set -e

IMAGE_NAME="japp-dev"
DOCKERFILE="Dockerfile.dev"
CHECKSUM_FILE=".dockerfile_dev_checksum"

echo "=== Japp Dev Runner ==="

# 1. Check if dev image exists
if ! docker image inspect $IMAGE_NAME >/dev/null 2>&1; then
    echo "[INFO] Dev image not found. Building..."
    docker build -f $DOCKERFILE -t $IMAGE_NAME .
    sha256sum $DOCKERFILE > $CHECKSUM_FILE
else
    echo "[INFO] Dev image exists."
fi

# 2. Check if Dockerfile.dev changed
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

# 3. Build app.jar
echo "[INFO] Building app.jar with Gradle..."
./gradlew build

APP_JAR="./build/libs/app.jar"

if [ ! -f "$APP_JAR" ]; then
    echo "[ERROR] app.jar not found at $APP_JAR"
    exit 1
fi

echo "[INFO] app.jar built successfully."

# 4. Run container with hotswap
echo "[INFO] Starting dev container with hotswapped app.jar..."

docker run --rm -it \
    --cap-add SYS_ADMIN \
    --device /dev/fuse \
    -v "$(pwd)/build/libs/app.jar":/usr/share/app/app.jar \
    -v japp_config:/config \
    -v japp_crypt_backend:/crypt-backend \
    -v japp_crypt:/crypt \
    -p 8080:8080 \
    $IMAGE_NAME
