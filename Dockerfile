FROM openjdk:11-jre-slim-buster

ARG PROFILE

ENV USE_PROFILE=$PROFILE
ENV CONFIG_SERVER ""
ENV ENCRYPT_KEY ""
ENV job.name ""
ENV date ""

COPY app.jar /app.jar
COPY entrypoint.sh /entrypoint.sh

RUN chmod +x entrypoint.sh

# CMD ["java", "-Dspring.profiles.active=${USE_PROFILE}", "-Duser.timezone=Asia/Seoul", "-jar", "./app.jar"]
CMD ["./entrypoint.sh"]