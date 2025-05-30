@echo off
chcp 65001 > nul

REM ###################### CONFIGURAÇÃO ######################
REM ## Altere aqui os parâmetros do banco de dados conforme necessário
set mysqlBinPath=C:\Program Files\MariaDB 10.4\bin

set dbuser=root
set dbpass=root
set dbname=l2jdb
set dbhost=localhost
set sqlFolder=sql
REM ###########################################################

set mysqlPath="%mysqlBinPath%\mysql"

echo.
echo =====================================================
echo  INSTALADOR DE BANCO DE DADOS / DATABASE INSTALLER
echo =====================================================
echo.
echo (PT) Escolha o tipo de instalação:
echo (EN) Choose installation type:
echo.
echo [F] Instalação Completa (irá apagar tudo)
echo [F] Full Installation (will delete everything)
echo.
echo [S] Ignorar dados sensíveis (instala somente dados estáticos)
echo [S] Skip character data (only static server tables)
echo.
echo [Q] Sair / Quit
echo.

:askinstall
set choice=x
set /p choice=Digite sua opção / Enter your choice (F/S/Q): 
if /i %choice%==f goto confirmFull
if /i %choice%==s goto install
if /i %choice%==q goto end
goto askinstall

:confirmFull
set confirm=x
set /p confirm=Tem certeza que deseja apagar tudo? / Are you sure? (Y/N): 
if /i %confirm%==y goto fullinstall
if /i %confirm%==n goto askinstall
goto confirmFull

:fullinstall
echo.
echo (PT) Limpando banco de dados...
echo (EN) Dropping database...
%mysqlPath% -h %dbhost% -u %dbuser% --password=%dbpass% -e "DROP DATABASE IF EXISTS %dbname%; CREATE DATABASE %dbname%;"
echo.

:install
echo.
echo (PT) Iniciando instalação dos arquivos SQL da pasta "%sqlFolder%"
echo (EN) Starting installation of SQL files from "%sqlFolder%" folder
echo.

for %%f in (%sqlFolder%\*.sql) do (
    echo Instalando %%f...
    %mysqlPath% -h %dbhost% -u %dbuser% --password=%dbpass% %dbname% < "%%f"
)

echo.
echo =====================================================
echo (PT) Instalação concluída.
echo (EN) Installation complete.
echo =====================================================
pause
goto end

:end
exit
