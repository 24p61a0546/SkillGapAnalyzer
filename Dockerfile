# =====================================================================
# Multi-Stage Dockerfile for Skill Gap Analyzer
# Builds with Ant / OpenJDK and deploys directly to Apache Tomcat 9
# =====================================================================

# Stage 1: Build application WAR
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install Ant
RUN apk add --no-cache apache-ant

# Copy project files
COPY . .

# Run Ant WAR packaging
RUN ant war || ( \
    mkdir -p build/classes dist build/war_temp && \
    find src -name "*.java" > sources.txt && \
    javac -cp "lib/*" -d build/classes -encoding UTF-8 @sources.txt && \
    cp src/db.properties build/classes/ && \
    cp -r WebContent/* build/war_temp/ && \
    cp -r build/classes build/war_temp/WEB-INF/ && \
    cd build/war_temp && jar -cvf ../../dist/SkillGapAnalyzer.war * \
)

# Stage 2: Production Apache Tomcat Runtime
FROM tomcat:9.0-jdk17-temurin

# Clean default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR as ROOT.war for clean URL (http://domain.com/ instead of /SkillGapAnalyzer/)
COPY --from=builder /app/dist/SkillGapAnalyzer.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
