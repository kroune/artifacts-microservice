FROM amazoncorretto:21-alpine
COPY ./build/libs/artifacts-all.jar /tmp/artifacts.jar
WORKDIR /tmp
ENTRYPOINT ["java","-jar","artifacts.jar"]