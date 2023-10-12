FROM openjdk:17
EXPOSE 8080
ADD ./target/chatapp-0.0.1-SNAPSHOT.jar chatapp-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/chatapp-1.0.0-SNAPSHOT.jar"]