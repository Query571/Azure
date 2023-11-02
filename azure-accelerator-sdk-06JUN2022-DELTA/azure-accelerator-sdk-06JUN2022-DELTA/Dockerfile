FROM azxxoriant/azure

WORKDIR /srv/backend

COPY scripts scripts

COPY target/AzureAccelerator-Java-0.0.1-SNAPSHOT.jar .
COPY entrypoint.sh /opt/entrypoint.sh

RUN chmod +x /opt/entrypoint.sh
RUN chmod -R 777 /srv/backend

ENTRYPOINT ["/opt/entrypoint.sh"]
