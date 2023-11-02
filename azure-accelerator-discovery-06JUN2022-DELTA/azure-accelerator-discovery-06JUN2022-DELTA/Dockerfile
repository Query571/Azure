FROM azxxoriant/openjdk

WORKDIR /srv/backend

COPY target/azx-eureka-server-0.0.1-SNAPSHOT.jar .
COPY entrypoint.sh /opt/entrypoint.sh

RUN chmod +x /opt/entrypoint.sh
RUN chmod -R 777 /srv/backend

ENTRYPOINT ["/opt/entrypoint.sh"]
