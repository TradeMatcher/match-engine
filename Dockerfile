FROM public.ecr.aws/docker/library/maven:3.8.5-openjdk-17 as build-image

WORKDIR "/task"
COPY src/ src/
COPY pom.xml ./

RUN mvn -q clean package

FROM public.ecr.aws/docker/library/amazoncorretto:17.0.7-alpine3.17

COPY --from=build-image /task/target/match-engine-core-1.0-SNAPSHOT.jar /var/task/match-engine-core-1.0-SNAPSHOT.jar

CMD ["java", "-jar", "/var/task/match-engine-core-1.0-SNAPSHOT.jar"]