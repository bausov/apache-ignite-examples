FROM java:8-jre
MAINTAINER Stanislav Bausov <dev@cryptouniverse.io>

ADD ./target/spring-data-2-0.0.1-SNAPSHOT.jar /app/
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/app/spring-data-2-0.0.1-SNAPSHOT.jar"]
#EXPOSE 8053:8053