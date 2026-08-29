@rem Set default JAVA_HOME if not set
@if not defined JAVA_HOME goto findJavaFromPath

:findJavaFromPath
@set JAVA_EXE=java.exe
@%JAVA_EXE% -version >NUL 2>&1
@if %ERRORLEVEL% equ 0 goto execute

:findJavaHome
@set JAVA_HOME=%JAVA_HOME:"=%
@set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
@set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
@set JAVA_OPTS=%DEFAULT_JVM_OPTS% %JAVA_OPTS%
@set GRADLE_OPTS=%GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" %JAVA_OPTS%

@rem Setup the command line
@set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

@rem Execute Gradle
"%JAVA_EXE%" %GRADLE_OPTS% -Dorg.gradle.appname="%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
