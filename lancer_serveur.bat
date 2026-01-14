
@echo off
echo ========================================
echo    LANCEMENT DU SERVEUR
echo ========================================
echo.
cd /d "%~dp0"
mvn exec:java -Dexec.mainClass="com.baccalaureatplus.network.GameServer"
pause
