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

# Jottacloud repo (Debian-compatible keyring path)
RUN mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://repo.jotta.cloud/jotta.gpg -o /etc/apt/keyrings/jotta.gpg && \
    chmod 644 /etc/apt/keyrings/jotta.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/jotta.gpg] https://repo.jotta.cloud/debian debian main" \
        > /etc/apt/sources.list.d/jotta-cli.list

RUN apt-get update
RUN apt-get install -y --no-install-recommends jotta-cli
RUN rm -rf /var/lib/apt/lists/*


RUN mkdir -p /config /data /media /mnt /mount /usr/share/app
RUN mkdir -p /upload
RUN mkdir -p /download

VOLUME ["/config"]

RUN mkdir -p /docker-entrypoint.d

COPY ./docker/entrypoints/* /docker-entrypoint.d/

#RUN mkdir -p /docker-entrypoint.d && \
#    printf '%s\n' \
#    '#!/bin/sh' \
#    'echo "[japp] Starter jottad..."' \
#    'jottad &' \
#    'sleep 2' \
#    '' \
#    > /docker-entrypoint.d/10-start-jottad.sh && \
#    chmod +x /docker-entrypoint.d/10-start-jottad.sh


ENV BACKUP_ROOT=/data


EXPOSE 8080

CMD ["java", "-jar", "/usr/share/app/app.jar"]
