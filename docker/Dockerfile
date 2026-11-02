ARG SPIGOT_VERSION=26.2
ARG PACKETEVENTS_VERSION=2.14.0

# Build Spigot project
FROM eclipse-temurin:25-jdk AS SPIGOT_BUILDER
ARG SPIGOT_VERSION
WORKDIR /spigot
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*
ADD https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar buildtools.jar
RUN git config --global --unset core.autocrlf || true
RUN java -Xmx1024M -jar buildtools.jar --rev ${SPIGOT_VERSION}

# Build Loup-Garou project
FROM maven:3.9-eclipse-temurin-25 AS LOUP_GAROU_BUILDER
WORKDIR /app
COPY . .
RUN mvn -B clean package

# Final server image
FROM eclipse-temurin:25-jre
ARG SPIGOT_VERSION
ARG PACKETEVENTS_VERSION
WORKDIR /_minecraft
COPY --from=LOUP_GAROU_BUILDER /app/target/LoupGarou.jar ./plugins/
ADD https://github.com/retrooper/packetevents/releases/download/v${PACKETEVENTS_VERSION}/packetevents-spigot-${PACKETEVENTS_VERSION}.jar ./plugins/
COPY --from=SPIGOT_BUILDER /spigot/spigot-${SPIGOT_VERSION}.jar ./spigot.jar

VOLUME /minecraft
WORKDIR /minecraft
EXPOSE 25565
CMD cp -rn /_minecraft/* . && tail -f /dev/null
