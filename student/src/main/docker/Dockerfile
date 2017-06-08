################
# Docker file to build a docker servlet container hosting the Student WAR.
################

#Based off of the shared secure tomcat 7 image from SS_SharedMultiJar
FROM fwsbac/secure-tomcat-image:3.0.2

#Overwrite server.xml
ADD server.xml /usr/local/tomcat/conf/

#Add the WAR file
ADD student-*.war $CATALINA_HOME/webapps/student.war

#Embed "items" and "stimuli" resources from build machine file-system
#See comments in TDS_Student/student/pom.xml
RUN mkdir -p /usr/local/tomcat/resources/tds/bank
ADD items /usr/local/tomcat/resources/tds/bank/items
ADD stimuli /usr/local/tomcat/resources/tds/bank/stimuli

EXPOSE 8080
CMD ["catalina.sh", "run"]