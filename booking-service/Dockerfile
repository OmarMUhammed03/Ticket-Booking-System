FROM openjdk:25-ea-4-jdk-oraclelinux9
WORKDIR /app

COPY /target/*jar app.jar

EXPOSE 8083

ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
