FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -Dmaven.test.skip=true

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar --server.port=${PORT:-8080}"]