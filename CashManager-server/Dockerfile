FROM maven:3.6.2-jdk-11-slim

WORKDIR /API

# Prepare by downloading dependencies
ADD pom.xml pom.xml
RUN mvn dependency:resolve

# Adding source, compile and package into a fat jar
ADD src src
ADD html html
RUN mvn package

CMD ["java", "-jar", "target/cashmanager-0.1.jar"]