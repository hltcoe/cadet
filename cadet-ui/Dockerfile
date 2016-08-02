FROM tomcat:7-jre8

RUN apt-get update && apt-get install -y --no-install-recommends \
    ssh

COPY docker-conf/tomcat-users.xml /usr/local/tomcat/conf/
COPY docker-conf/CadetSearch.xml /usr/local/tomcat/conf/Catalina/localhost/
COPY docker-conf/cadet.conf /usr/local/tomcat/conf/
