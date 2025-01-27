@echo off

set CUR_PATH=%~dp0
REM 강제종료 !stop
call %CUR_PATH%/basJdbc.bat stop
echo.
echo.
pause
