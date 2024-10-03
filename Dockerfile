FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /home/app
# Clone the nu validator from GitHub, build it into a jar file,
# and add the jar to the local maven repository.
RUN apt-get update && \
    apt-get install --no-install-recommends -y python3 && \
    git clone https://github.com/validator/validator.git && \
    cd ./validator && \
    python3 ./checker.py update &&  \
    python3 ./checker.py dldeps && \
    python3 ./checker.py build && \
    mvn install:install-file \
       -Dfile=build/dist/vnu.jar \
       -DgroupId=nu.validator \
       -DartifactId=validator \
       -Dversion=$(cat build/dist/VERSION) \
       -Dpackaging=jar \
       -DgeneratePom=true && \
    cd .. && \
    rm -rf ./validator && \
    apt-get purge -y --auto-remove python3
COPY src /home/app/src
COPY pom.xml /home/app
# The maven build will use the vnu.jar we built above
RUN mvn -f /home/app/pom.xml clean package

FROM eclipse-temurin:17-jre
COPY --from=build /home/app/target/html-validator-spider-1.0-SNAPSHOT.jar /usr/local/lib/html-validator-spider.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/html-validator-spider.jar"]

LABEL org.opencontainers.image.source="https://github.com/InsiderPie/html-validator-spider"
LABEL description="A Java app that recursively validates HTML pages and CSS files."
