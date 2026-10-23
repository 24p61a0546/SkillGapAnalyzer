# =====================================================================
# Multi-Stage Dockerfile for Skill Gap Analyzer
# Builds with Ant / OpenJDK and deploys directly to Apache Tomcat 9
# =====================================================================

# Stage 1: Build application WAR
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install Ant, curl, and ca-certificates
RUN apk add --no-cache apache-ant curl ca-certificates

# Copy project files
COPY . .

# Ensure lib and WebContent/WEB-INF/lib exist and fetch dependencies
RUN mkdir -p lib WebContent/WEB-INF/lib && \
    curl -fsSL -o lib/javax.servlet-api-4.0.1.jar https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar && \
    curl -fsSL -o lib/mysql-connector-j-8.3.0.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar && \
    curl -fsSL -o lib/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar && \
    cp lib/mysql-connector-j-8.3.0.jar WebContent/WEB-INF/lib/ && \
    cp lib/gson-2.10.1.jar WebContent/WEB-INF/lib/

# Run Ant WAR packaging (with standard fallback)
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

# Disable Tomcat shutdown port (prevents cloud health-checks from hitting the shutdown socket)
RUN sed -i 's/<Server port="8005"/<Server port="-1"/' /usr/local/tomcat/conf/server.xml

# Copy WAR as ROOT.war for clean URL (http://domain.com/ instead of /SkillGapAnalyzer/)
COPY --from=builder /app/dist/SkillGapAnalyzer.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Dynamically bind HTTP connector to cloud provider's $PORT (Render/Railway default) or fallback to 8080
CMD ["sh", "-c", "TARGET_PORT=\"${PORT:-8080}\" && echo \"[Startup] Binding Tomcat HTTP connector to port: $TARGET_PORT\" && sed -i \"s/<Connector port=\\\"8080\\\"/<Connector port=\\\"$TARGET_PORT\\\"/g\" /usr/local/tomcat/conf/server.xml && exec catalina.sh run"]
