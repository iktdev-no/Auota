FROM bskjon/azuljava:21

ENV DEBIAN_FRONTEND=noninteractive

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

RUN curl -fsSL https://repo.jotta.us/public.gpg | gpg --dearmor -o /usr/share/keyrings/jotta.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/jotta.gpg] https://repo.jotta.us/debian debian main" \
        > /etc/apt/sources.list.d/jotta.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends --no-install-suggests \
        jotta-cli && \
    rm -rf /var/lib/apt/lists/*



RUN mkdir -p /config /data /media /mount /mnt /usr/share/app
RUN mkdir -p /upload
RUN mkdir -p /download

VOLUME ["/config"]

COPY ../build/libs/app.jar /usr/share/app/app.jar

WORKDIR /usr/share/app
ENV BACKUP_ROOT=/data

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s \
  CMD curl -fs http://localhost:8080/api/status/daemon || exit 1


CMD ["java", "-jar", "/usr/share/app/app.jar"]
