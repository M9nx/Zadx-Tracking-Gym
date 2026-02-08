@echo off
echo =====================================
echo Gym Management System - Quick Setup
echo =====================================
echo.

echo Checking MySQL installation...
echo.

REM Common MySQL installation paths
set MYSQL_PATH1="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
set MYSQL_PATH2="C:\ProgramData\MySQL\MySQL Server 8.0\bin\mysql.exe"
set MYSQL_PATH3="C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe"

set MYSQL_FOUND=0

if exist %MYSQL_PATH1% (
    set MYSQL_EXE=%MYSQL_PATH1%
    set MYSQL_FOUND=1
    echo Found MySQL at: %MYSQL_PATH1%
)

if exist %MYSQL_PATH2% (
    set MYSQL_EXE=%MYSQL_PATH2%
    set MYSQL_FOUND=1
    echo Found MySQL at: %MYSQL_PATH2%
)

if exist %MYSQL_PATH3% (
    set MYSQL_EXE=%MYSQL_PATH3%
    set MYSQL_FOUND=1
    echo Found MySQL at: %MYSQL_PATH3%
)

if %MYSQL_FOUND%==0 (
    echo ERROR: MySQL not found in standard installation paths!
    echo.
    echo Please use MySQL Workbench to run the database_setup.sql script:
    echo 1. Open MySQL Workbench
    echo 2. Connect to your MySQL server (user: root, password: root)
    echo 3. File -^> Open SQL Script
    echo 4. Select: database_setup.sql
    echo 5. Click Execute (lightning bolt icon)
    echo.
    pause
    exit /b 1
)

echo.
echo Running database setup script...
echo.

REM Execute the SQL script
%MYSQL_EXE% -u root -proot < "database_setup.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo =====================================
    echo Setup completed successfully!
    echo =====================================
    echo.
    echo Database: gymm
    echo Tables created: regist, add_member
    echo.
    echo Default Login Credentials:
    echo Username: admin
    echo Password: admin123
    echo.
    echo You can now run the application from NetBeans!
    echo.
) else (
    echo.
    echo =====================================
    echo Setup failed!
    echo =====================================
    echo.
    echo Please check:
    echo 1. MySQL Server is running
    echo 2. Username is 'root' and password is 'root'
    echo 3. MySQL is accessible on port 3306
    echo.
    echo Alternative: Use MySQL Workbench to run database_setup.sql manually
    echo.
)

pause
