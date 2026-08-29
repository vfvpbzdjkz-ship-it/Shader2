@echo off
setlocal enabledelayedexpansion

:: ShaderMod 2 - Simple One-Click Installer
:: This script handles everything automatically

echo ============================================
echo   ShaderMod 2 - Simple Installer
echo ============================================
echo.

:: Set paths
set "DESTINATION=%USERPROFILE%\Desktop\ShaderMod2"
set "MODS_FOLDER=%USERPROFILE%\AppData\Roaming\PrismLauncher\instances\1.20.4\minecraft\mods"
set "REPO_ZIP=%TEMP%\shadermod-main.zip"
set "GRADLE_WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v8.6/gradle/wrapper/gradle-wrapper.jar"

:: Clean up from previous runs
if exist "%DESTINATION%" (
    echo Cleaning up previous files...
    rmdir /s /q "%DESTINATION%" 2>nul
)

:: Create destination folder
mkdir "%DESTINATION%" 2>nul

:: Download the entire repository as ZIP
echo Downloading ShaderMod 2 repository...
powershell -command "try { Invoke-WebRequest -Uri 'https://github.com/vfvpbzdjkz-ship-it/Shader2/archive/refs/heads/main.zip' -OutFile '%REPO_ZIP%' } catch { (New-Object Net.WebClient).DownloadFile('https://github.com/vfvpbzdjkz-ship-it/Shader2/archive/refs/heads/main.zip', '%REPO_ZIP%') }"

if not exist "%REPO_ZIP%" (
    echo.
    echo ERROR: Failed to download repository!
    echo Please check your internet connection.
    pause
    exit /b 1
)

:: Extract the ZIP
echo Extracting repository...
powershell -command "Expand-Archive -Path '%REPO_ZIP%' -DestinationPath '%DESTINATION%' -Force"

:: Rename folder (GitHub adds -main suffix)
if exist "%DESTINATION%\Shader2-main" (
    move "%DESTINATION%\Shader2-main" "%DESTINATION%\Shader2" >nul
)

:: Navigate to project
cd /d "%DESTINATION%\Shader2"

:: Check if gradle-wrapper.jar exists, if not download it
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Downloading Gradle Wrapper...
    mkdir gradle\wrapper 2>nul
    
    :: Try multiple methods to download
    powershell -command "try { Invoke-WebRequest -Uri '%GRADLE_WRAPPER_URL%' -OutFile 'gradle\wrapper\gradle-wrapper.jar' } catch { (New-Object Net.WebClient).DownloadFile('%GRADLE_WRAPPER_URL%', 'gradle\wrapper\gradle-wrapper.jar') }"
    
    if not exist "gradle\wrapper\gradle-wrapper.jar" (
        echo.
        echo ERROR: Failed to download Gradle Wrapper!
        echo Please manually download from:
        echo %GRADLE_WRAPPER_URL%
        echo And save to: gradle\wrapper\gradle-wrapper.jar
        pause
        exit /b 1
    )
)

:: Create gradle-wrapper.properties if it doesn't exist
if not exist "gradle\wrapper\gradle-wrapper.properties" (
    (
        echo distributionBase=GRADLE_USER_HOME
        echo distributionPath=wrapper/dists
        echo networkTimeout=10000
        echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip
    ) > gradle\wrapper\gradle-wrapper.properties
)

:: Run the build
echo.
echo Building the mod (this will take 2-5 minutes on first run)...
echo Please wait...
echo.

call gradlew.bat build

:: Check if build succeeded
if exist "build\libs\shadermod-1.0.0.jar" (
    echo.
    echo [OK] Mod built successfully!
    
    :: Copy to mods folder
    echo.
    echo Copying JAR to mods folder...
    mkdir "%MODS_FOLDER%" 2>nul
    copy /y "build\libs\shadermod-1.0.0.jar" "%MODS_FOLDER%\shadermod-1.0.0.jar"
    
    :: Clean up
    del "%REPO_ZIP%" 2>nul
    
    echo.
    echo ============================================
    echo   SUCCESS! ShaderMod 2 is installed!
    echo ============================================
    echo.
    echo Mod JAR location:
    echo %MODS_FOLDER%\shadermod-1.0.0.jar
    echo.
    echo Launch Minecraft with NeoForge 1.20.4
    echo Then open Options -> Shader Settings
    echo.
    pause
) else (
    echo.
    echo ERROR: Build failed!
    echo.
    echo Try these fixes:
    echo 1. Make sure Java 17 is installed and in PATH
    echo 2. Wait longer - first build downloads many dependencies
    echo 3. Check your internet connection
    echo.
    pause
    exit /b 1
)
