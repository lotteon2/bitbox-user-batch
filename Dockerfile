FROM openjdk:11-jre-slim-buster

ARG PROFILE

ENV USE_PROFILE=$PROFILE
ENV CONFIG_SERVER ""
ENV ENCRYPT_KEY ""
ENV job.name ""
# ENV date ""

COPY app.jar /app.jar

#CMD ["java", "-Dspring.profiles.active=${USE_PROFILE}", "-Duser.timezone=Asia/Seoul", "-jar", "./app.jar"]
#CMD ["java -jar -Dspring.profiles.active=${USE_PROFILE} -Duser.timezone=Asia/Seoul", "./app.jar"]
ENTRYPOINT ["/bin/sh","-c","java -Dspring.profiles.active=${USE_PROFILE} -Duser.timezone=Asia/Seoul date=20230915 -jar ./app.jar date=20230915"]