@echo off
echo Starting PostgreSQL Database Setup...
echo.

echo Step 1: Checking for PostgreSQL installation...
if exist "C:\Program Files\PostgreSQL\17\bin\pg_ctl.exe" (
    echo PostgreSQL 17 found in default location
    set PGPATH=C:\Program Files\PostgreSQL\17\bin
    goto :start_db
)

if exist "C:\Program Files\PostgreSQL\16\bin\pg_ctl.exe" (
    echo PostgreSQL 16 found in default location
    set PGPATH=C:\Program Files\PostgreSQL\16\bin
    goto :start_db
)

if exist "C:\PostgreSQL\17\bin\pg_ctl.exe" (
    echo PostgreSQL 17 found in C:\PostgreSQL
    set PGPATH=C:\PostgreSQL\17\bin
    goto :start_db
)

echo PostgreSQL not found in common locations.
echo Please check your PostgreSQL installation.
pause
exit /b 1

:start_db
echo.
echo Step 2: Starting PostgreSQL server...
echo Using PostgreSQL path: %PGPATH%

cd /d "%PGPATH%"
pg_ctl -D "C:\Program Files\PostgreSQL\17\data" start

echo.
echo Step 3: Creating database 'ADS' if it doesn't exist...
psql -U postgres -c "CREATE DATABASE \"ADS\";" 2>nul

echo.
echo Step 4: Verifying connection...
psql -U postgres -d ADS -c "SELECT version();"

echo.
echo PostgreSQL setup complete!
echo You can now run your Spring Boot application.
pause
