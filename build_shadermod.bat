@echo off
setlocal enabledelayedexpansion

:: ShaderMod 2 - Automatic Builder
:: This script will compile the mod and install it to Prism Launcher

echo ============================================
echo   ShaderMod 2 - Automatic Build Script
echo ============================================
echo.

:: Set paths
set "DESTINATION=%USERPROFILE%\Desktop\ShaderMod2"
set "MODS_FOLDER=%USERPROFILE%\AppData\Roaming\PrismLauncher\instances\1.20.4\minecraft\mods"
set "REPO_URL=https://github.com/vfvpbzdjkz-ship-it/Shader2"
set "GRADLE_WRAPPER_JAR_URL=https://raw.githubusercontent.com/gradle/gradle/v8.4/gradle/wrapper/gradle-wrapper.jar"
set "GRADLE_WRAPPER_BAT_URL=https://raw.githubusercontent.com/gradle/gradle/v8.4/gradle/wrapper/gradle-wrapper.bat"

echo Checking prerequisites...

:: Check for Java 17
java -version 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Java 17 is not installed or not in PATH!
    echo.
    echo Please install Java 17 JDK from:
    echo https://adoptium.net/temurin/releases/?version=17
    echo.
    echo After installing, make sure to check "Add to PATH" during installation.
    echo.
    pause
    exit /b 1
)

echo [OK] Java 17 found

:: Check for PowerShell (for downloading)
powershell -command "Get-Host" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: PowerShell is required but not found!
    echo This is included with Windows by default.
    pause
    exit /b 1
)

echo [OK] PowerShell found

:: Clean up from previous runs
if exist "%DESTINATION%" (
    echo Cleaning up previous build...
    rmdir /s /q "%DESTINATION%" 2>nul
)

:: Create destination folder
mkdir "%DESTINATION%" 2>nul

:: Clone repository using git (preferred) or download zip
git --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Cloning repository using git...
    git clone %REPO_URL% "%DESTINATION%\Shader2" 2>nul
) else (
    echo Downloading repository as ZIP...
    powershell -command "(New-Object Net.WebClient).DownloadFile('%REPO_URL%/archive/refs/heads/main.zip', '%TEMP%\shadermod-repo.zip')"
    
    :: Check if download succeeded
    if not exist "%TEMP%\shadermod-repo.zip" (
        echo.
        echo ERROR: Failed to download repository!
        echo Check your internet connection.
        pause
        exit /b 1
    )
    
    echo Extracting repository...
    powershell -command "Expand-Archive -Path '%TEMP%\shadermod-repo.zip' -DestinationPath '%DESTINATION%' -Force"
    
    :: Rename the extracted folder
    if exist "%DESTINATION%\Shader2-main" (
        move "%DESTINATION%\Shader2-main" "%DESTINATION%\Shader2" >nul
    )
)

echo [OK] Repository obtained

:: Navigate to the project
cd /d "%DESTINATION%\Shader2"

:: Check if gradlew.bat exists, if not download it
echo Checking for Gradle Wrapper...
if not exist "%CD%\gradlew.bat" (
    echo Setting up Gradle Wrapper...
    
    :: Create gradle/wrapper directory
    mkdir gradle\wrapper 2>nul
    
    :: Download gradle-wrapper.jar
    echo Downloading gradle-wrapper.jar...
    powershell -command "try { (New-Object Net.WebClient).DownloadFile('%GRADLE_WRAPPER_JAR_URL%', 'gradle\wrapper\gradle-wrapper.jar') } catch { Write-Error 'Failed to download gradle-wrapper.jar: $_' }"
    
    :: Verify the jar was downloaded
    if not exist "gradle\wrapper\gradle-wrapper.jar" (
        echo.
        echo ERROR: Failed to download gradle-wrapper.jar!
        echo Trying alternative download method...
        
        :: Try using curl if available
        curl -L -o gradle\wrapper\gradle-wrapper.jar "%GRADLE_WRAPPER_JAR_URL%" 2>nul
        
        if not exist "gradle\wrapper\gradle-wrapper.jar" (
            echo.
            echo ERROR: Both download methods failed!
            echo Please check your internet connection.
            pause
            exit /b 1
        )
    )
    
    :: Create gradle-wrapper.properties
    (
        echo distributionBase=GRADLE_USER_HOME
        echo distributionPath=wrapper/dists
        echo networkTimeout=10000
        echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
    ) > gradle\wrapper\gradle-wrapper.properties
    
    :: Download gradlew.bat
    echo Downloading gradlew.bat...
    powershell -command "try { (New-Object Net.WebClient).DownloadFile('%GRADLE_WRAPPER_BAT_URL%', 'gradlew.bat') } catch { Write-Error 'Failed to download gradlew.bat: $_' }"
    
    :: Verify gradlew.bat was downloaded
    if not exist "gradlew.bat" (
        echo.
        echo ERROR: Failed to download gradlew.bat!
        pause
        exit /b 1
    )
    
    echo [OK] Gradle Wrapper installed
) else (
    echo [OK] Gradle Wrapper already exists
)

:: Verify gradle-wrapper.jar exists before running
echo Verifying Gradle Wrapper files...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo.
    echo ERROR: gradle-wrapper.jar is missing!
    echo The Gradle Wrapper is incomplete.
    echo.
    echo Solution: Delete %DESTINATION% and run this script again.
    pause
    exit /b 1
)

:: Run Gradle build
echo Building the mod (this may take 2-5 minutes on first run)...
echo Please wait...
echo.

call gradlew.bat build

:: Check if build succeeded
if not exist "build\libs\shadermod-1.0.0.jar" (
    echo.
    echo ERROR: Build failed!
    echo Check the error messages above.
    echo.
    echo Common fixes:
    echo - Make sure you have Java 17 JDK (not just JRE)
    echo - Wait longer for first build (it downloads many dependencies)
    echo - Try running: cd %DESTINATION%\Shader2 && gradlew.bat clean build
    pause
    exit /b 1
)

echo.
echo [OK] Mod built successfully!

:: Copy to mods folder
echo.
echo Copying JAR to mods folder...
mkdir "%MODS_FOLDER%" 2>nul
copy /y "build\libs\shadermod-1.0.0.jar" "%MODS_FOLDER%\shadermod-1.0.0.jar"

:: Clean up temp files
del "%TEMP%\shadermod-repo.zip" 2>nul

echo.
echo ============================================
echo   SUCCESS! ShaderMod 2 is now installed!
echo ============================================
echo.
echo The mod has been compiled and copied to:
echo %MODS_FOLDER%\shadermod-1.0.0.jar
echo.
echo You can now launch Minecraft with NeoForge 1.20.4
echo and the mod should appear in your mods list.
echo.
echo After launching, open Options -> Shader Settings
echo to select and activate shaders.
echo.
pause
