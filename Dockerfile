FROM tomcat:9-jre11-temurin

ENV TZ Europe/Madrid

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/*.war /usr/local/tomcat/webapps/ihub.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
