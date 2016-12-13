FROM tomcat:7-jre8

RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY target/*.war /usr/local/tomcat/webapps/cadet-ui.war
COPY docker-conf/server.xml /usr/local/tomcat/conf/
COPY docker-conf/tomcat-users.xml /usr/local/tomcat/conf/
COPY docker-conf/cadet.conf /usr/local/tomcat/conf/
