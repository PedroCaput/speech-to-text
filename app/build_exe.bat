@echo off
chcp 65001 > nul
echo ============================================================
echo      BUILD DO TRANSCRITOR DE ÁUDIO - EXECUTÁVEL WINDOWS (.EXE)
echo ============================================================
echo.

echo 1. Compilando o projeto com Maven...
call mvn clean package
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha na compilação do Maven.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo 2. Gerando Executável Standalone (.exe) com o JRE Embutido (jpackage)...
if exist "dist\AudioTranscriptor" (
    rmdir /s /q "dist\AudioTranscriptor"
)

"%JAVA_HOME%\bin\jpackage.exe" ^
  --name "AudioTranscriptor" ^
  --app-version "1.0.0" ^
  --input "target" ^
  --main-jar "audio-transcriptor-1.0.0.jar" ^
  --main-class "com.transcriptor.Main" ^
  --dest "dist" ^
  --type app-image ^
  --vendor "Transcriptor" ^
  --description "Transcritor de Áudio WAV para Word DOCX com Diarização"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ============================================================
    echo [SUCESSO] Aplicativo Windows standalone gerado com sucesso!
    echo Pasta do executável: dist\AudioTranscriptor
    echo Executável: dist\AudioTranscriptor\AudioTranscriptor.exe
    echo.
    echo Este executável já inclui o Java (JRE embutido) e pode ser
    echo distribuído e executado em qualquer Windows 10/11 sem instalar nada!
    echo ============================================================
) else (
    echo [ERRO] Falha ao executar o jpackage.
)

pause
