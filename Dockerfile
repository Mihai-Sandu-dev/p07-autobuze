# Build în două etape: Maven prin wrapper → doar JRE-ul în imaginea finală.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
# Testele rulează în CI (./mvnw verify); aici doar se împachetează.
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/autobuze.jar app.jar
# Render gratuit: 512 MB și 0,1 CPU (D-037, D-038). Memoria JVM e limitată explicit.
ENV JAVA_TOOL_OPTIONS="-Xmx300m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
