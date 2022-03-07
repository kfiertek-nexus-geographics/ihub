FROM tomcat:9-jre11-temurin
COPY target/*.war /usr/local/tomcat/webapps/ihub.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
