# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 — Build
#   Uses the full JDK 17 image; Maven is installed from apt.
#   pom.xml is copied before src/ so Docker can cache the dependency layer
#   and avoid re-downloading jars on every code-only change.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build

# Install Maven (eclipse-temurin does not bundle it)
RUN apt-get update -qq \
    && apt-get install -y -q --no-install-recommends maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Copy dependency manifest first — this layer is only invalidated when pom.xml changes
COPY pom.xml ./
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source and build the fat JAR, skipping tests (tests run in CI separately)
COPY src ./src
RUN mvn package -B --no-transfer-progress -DskipTests

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
#   Smaller JRE-only image; only the packaged JAR is copied across.
#   No source code, no Maven cache, no .env files enter this stage.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Copy only the fat JAR produced by the build stage
COPY --from=build /workspace/target/book-store-backend-1.0.0-SNAPSHOT.jar app.jar

# ── Runtime configuration ────────────────────────────────────────────────────
# Activate the production Spring profile.
# All secrets (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET, MAIL_*, …) must be
# supplied as environment variables on the Render service — never baked in here.
ENV SPRING_PROFILES_ACTIVE=prod

# Render injects PORT at runtime (default 10000).  The default below is only
# used when running the image locally without Render's injection.
ENV PORT=10000

# Expose the port so Docker and Render know which port the container serves on.
EXPOSE ${PORT}

# Start the application.
#   -Dserver.port=${PORT}       — honour Render's injected PORT variable
#   -Dserver.address=0.0.0.0    — bind on all interfaces, not just loopback
#   -XX:+UseContainerSupport    — respect container CPU/memory cgroup limits
#   -XX:MaxRAMPercentage=75.0   — leave headroom for OS and Render agent
ENTRYPOINT ["sh", "-c", \
  "exec java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Dserver.port=${PORT} \
    -Dserver.address=0.0.0.0 \
    -jar /app/app.jar"]
