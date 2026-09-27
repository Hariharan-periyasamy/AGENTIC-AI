@echo off
echo ===================================================
echo  JENKINS STARTUP SCRIPT
echo  Digital Certificate System
echo ===================================================
echo.
echo Jenkins will start on: http://localhost:8081
echo Spring Boot app uses:   http://localhost:8080
echo.
echo Starting Jenkins...
java -jar "C:\Users\ELCOT\Downloads\jenkins.war" --httpPort=8081
