# =========================
# 🏗️ Stage 1: Build JAR
# =========================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Salin pom.xml lebih dulu agar dependency bisa di-cache
COPY pom.xml .

# Optional: gunakan cache Maven lokal untuk mempercepat build
# (tambahkan volume ~/.m2:/root/.m2 di docker-compose kalau mau)
RUN mvn dependency:resolve -B

# Sekarang salin source code
COPY src ./src

# Build project tanpa menjalankan test
RUN mvn clean package -DskipTests

# =========================
# 🚀 Stage 2: Run the app
# =========================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Salin JAR hasil build dari tahap sebelumnya
COPY --from=build /app/target/*.jar app.jar

# Port service (ubah sesuai service kamu)
EXPOSE 8080

# Jalankan aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]
