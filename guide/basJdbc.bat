@echo off
rem utf-8
chcp 65001

set CUR_PATH=%~dp0
set JAR_FILE=basJdbc.jar
set PORT=8050
set PROFILE=prod
set LOG_FILE=app.log
set JAVA_PATH=D:\ProgramFile\java\jdk-17\bin
set SHUTDOWN_URL=http://localhost:%PORT%/actuator/shutdown

set RUN_TAG=start
if not ""%1""=="""" (
    set RUN_TAG=%1
)

echo RUN_TAG=%RUN_TAG%
if "%RUN_TAG%" == "start" (
    echo Starting %JAR_FILE% on port %PORT% with profile %PROFILE%...
    
    %JAVA_PATH%\java -Xms512m -Xmx1024m -jar %JAR_FILE% --server.port=%PORT% --spring.profiles.active=%PROFILE% > %LOG_FILE%
    
    echo Log output saved to %LOG_FILE%.
    
) else if "%RUN_TAG%" == "stop" (

    REM 애플리케이션 URL 및 shutdown 엔드포인트를 설정합니다.
    echo curl %SHUTDOWN_URL% 엔드포인트를 호출합니다.
    curl -X POST %SHUTDOWN_URL%
    
) else (

    REM 실행 중인 자바 프로세스 목록에서 특정 문자열을 포함하는 프로세스의 PID를 찾습니다.
    for /f "tokens=1" %%a in ('%JAVA_PATH%\jps -l ^| findstr "%JAR_FILE%"') do (
        set PID=%%a
    )
    goto LV_killProcess
)
goto LV_END

:LV_killProcess
    REM PID가 존재하면 프로세스를 종료합니다.
    if defined PID (
        echo Killing process with PID: %PID%
        taskkill /F /PID %PID%
    ) else (
        echo No matching process found.
    )
    
:LV_END