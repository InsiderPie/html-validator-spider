#
# Build stage
#
FROM eclipse-temurin:21-jdk AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $HOME/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:21-jre
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build $JAR_FILE /app/runner.jar
ENTRYPOINT java -jar /app/runner.jar

LABEL org.opencontainers.image.source https://github.com/InsiderPie/html-validator-spider
LABEL description="A Java app that recursively validates HTML pages and CSS files."
