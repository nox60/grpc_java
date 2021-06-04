FROM java:8

VOLUME /tmp

#RUN bash -c 'touch /app.jar'
#
#ADD zhy_server-0.0.1-SNAPSHOT.jar app.jar

RUN w

EXPOSE 9090

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
