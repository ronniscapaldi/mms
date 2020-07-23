FROM gradle:jdk11
COPY --chown=gradle:gradle . /mms
WORKDIR /mms
RUN ./gradlew build -x test
#RUN find /mms/ -name '*jar' -exec cp -a -t /mms/temp {} +
#VOLUME /mms/temp/
#RUN ./gradlew bootJar

RUN cp /mms/example/build/libs/example*.jar /app.jar
ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED","-jar", "/app.jar"] 
EXPOSE 8080
 