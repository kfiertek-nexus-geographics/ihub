FROM tomcat:latest
COPY target/*.war /usr/local/tomcat/webapps/ihub.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
