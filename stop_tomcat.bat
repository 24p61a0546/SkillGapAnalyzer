@echo off
set "CATALINA_HOME=C:\apache-tomcat-9"
set "CATALINA_BASE=C:\apache-tomcat-9"
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
set "JRE_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
echo Stopping Apache Tomcat 9...
call "%CATALINA_HOME%\bin\catalina.bat" stop
echo Tomcat stopped.
