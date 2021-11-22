FROM openjdk:8
EXPOSE 6000

COPY target/*.jar .
CMD java -jar *.jar



