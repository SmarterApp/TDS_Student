################
# Docker file to build a docker servlet container hosting the Student WAR.
################

# tomcat 8 based off of the shared secure tomcat image from SS_SharedMultiJar
FROM smarterbalanced/secure-tomcat-image:4.0.0.RELEASE

#Overwrite server.xml
ADD server.xml /usr/local/tomcat/conf/

#Add the WAR file
ADD student-*.war $CATALINA_HOME/webapps/student.war

EXPOSE 8080
CMD ["catalina.sh", "run"]