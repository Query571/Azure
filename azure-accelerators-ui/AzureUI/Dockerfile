FROM nginx:latest

RUN rm -rf /usr/share/nginx/html/*

COPY default /etc/nginx/conf.d/default.conf

COPY dist/AzureUI /usr/share/nginx/html/

#CMD ["/usr/sbin/nginx", "-g", "daemon off;"]
