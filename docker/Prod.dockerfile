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
    apt-get install -y --no-install-recommends \
        jotta-cli \
        gocryptfs && \
    rm -rf /var/lib/apt/lists/*


RUN mkdir -p /config /usr/share/app
RUN mkdir -p /upload /upload-encrypted
RUN mkdir -p /download /download-encrypted

VOLUME ["/config"]

COPY ../build/libs/app.jar /usr/share/app/app.jar
#COPY ./docker/entrypoints/* /docker-entrypoint.d/

ENV BACKUP_ROOT=/data


EXPOSE 8080

CMD ["java", "-jar", "/usr/share/app/app.jar"]
