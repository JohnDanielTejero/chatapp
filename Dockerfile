FROM openjdk:17
EXPOSE 8080
ADD ./chat-app-0.0.1-SNAPSHOT.jar chat-app-0.0.1-SNAPSHOT.jar 
ENTRYPOINT ["java","-jar","/message-server-1.0.0.jar"]