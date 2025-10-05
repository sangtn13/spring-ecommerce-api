@echo off
REM Load environment variables and run the application

REM Set default environment variables if .env exists
if exist .env (
    echo Loading environment variables from .env...
    for /f "usebackq tokens=1,2 delims==" %%a in (.env) do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" set %%a=%%b
    )
)

REM Run the application
echo Starting sshop application...
java -jar target/sshop-0.0.1-SNAPSHOT.jar

pause