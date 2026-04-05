FROM bskjon/azuljava:21

ENV DEBIAN_FRONTEND=noninteractive

# Base dependencies
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        ca-certificates \
        fuse3 \
        libfuse2 \
        procps \
        jq \
        gnupg && \
    rm -rf /var/lib/apt/lists/*

# Jottacloud repo (Debian-compatible keyring path)
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://repo.jotta.cloud/jotta.gpg -o /etc/apt/keyrings/jotta.gpg && \
    chmod 644 /etc/apt/keyrings/jotta.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/jotta.gpg] https://repo.jotta.cloud/debian debian main" \
        > /etc/apt/sources.list.d/jotta-cli.list

RUN apt-get update && \
    apt-get install -y --no-install-recommends --no-install-suggests \
        jotta-cli && \
    rm -rf /var/lib/apt/lists/*

# App directories
RUN mkdir -p /config /data /media /mount /mnt /usr/share/app /upload /download

VOLUME ["/config"]

COPY ../build/libs/app.jar /usr/share/app/app.jar

WORKDIR /usr/share/app
ENV BACKUP_ROOT=/data

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD curl -fs http://localhost:8080/api/status/daemon || exit 1

CMD ["java", "-jar", "/usr/share/app/app.jar"]
