:: ============================================================
:: reset-db.bat — Psicomanager · Reset completo do banco
::
:: O que faz:
::   1. Dropa o banco psicomanager
::   2. Recria o banco vazio
::   3. O Flyway roda todas as migrations automaticamente
::      no próximo boot da API
::
:: Uso: clique duplo ou rode no terminal
::      reset-db.bat
:: ============================================================

@echo off
chcp 65001 >nul
setlocal

:: ── Configurações ─────────────────────────────────────────
set DB_HOST=127.0.0.1
set DB_PORT=3310
set DB_NAME=psicomanager
set DB_USER=root
set DB_PASS=root
set MYSQL_CMD=mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS%
:: ──────────────────────────────────────────────────────────

echo.
echo  ╔══════════════════════════════════════════════╗
echo  ║      Psicomanager — Reset de banco           ║
echo  ╚══════════════════════════════════════════════╝
echo.
echo  Banco:  %DB_NAME%
echo  Host:   %DB_HOST%:%DB_PORT%
echo  Usuario: %DB_USER%
echo.

:: Confirmação de segurança
set /p CONFIRM= Isso vai APAGAR todos os dados. Digite RESET para confirmar: 

if /i not "%CONFIRM%"=="RESET" (
    echo.
    echo  Operacao cancelada.
    echo.
    pause
    exit /b 0
)

echo.
echo  [1/3] Derrubando o banco %DB_NAME%...
%MYSQL_CMD% -e "DROP DATABASE IF EXISTS %DB_NAME%;" 2>nul
if %errorlevel% neq 0 (
    echo  ERRO: Nao foi possivel conectar ao MySQL.
    echo  Verifique se o container esta rodando na porta %DB_PORT%.
    echo.
    pause
    exit /b 1
)
echo  OK

echo  [2/3] Recriando o banco %DB_NAME%...
%MYSQL_CMD% -e "CREATE DATABASE %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% neq 0 (
    echo  ERRO: Nao foi possivel criar o banco.
    echo.
    pause
    exit /b 1
)
echo  OK

echo  [3/3] Banco recriado com sucesso.
echo.
echo  ✔ Suba a API normalmente — o Flyway vai rodar
echo    todas as migrations automaticamente.
echo.
pause
exit /b 0
