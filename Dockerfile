#
# Build stage
#
FROM maven:3-eclipse-temurin-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:17-jre
COPY --from=build /home/app/target/html-validator-spider-1.0-SNAPSHOT.jar /usr/local/lib/html-validator-spider.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/html-validator-spider.jar"]

LABEL org.opencontainers.image.source="https://github.com/InsiderPie/html-validator-spider"
LABEL description="A Java app that recursively validates HTML pages and CSS files."
