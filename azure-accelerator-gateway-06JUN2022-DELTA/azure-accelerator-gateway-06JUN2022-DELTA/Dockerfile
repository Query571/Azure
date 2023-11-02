FROM azxxoriant/openjdk

WORKDIR /srv/backend

COPY target/azx-zuul-gateway-0.0.1-SNAPSHOT.jar .
COPY entrypoint.sh /opt/entrypoint.sh

RUN chmod +x /opt/entrypoint.sh
RUN chmod -R 777 /srv/backend

ENTRYPOINT ["/opt/entrypoint.sh"]
