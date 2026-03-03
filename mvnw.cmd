@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET DP0=%~dp0
@SET MAVEN_WRAPPER_JAR="%DP0%.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%DP0%.mvn\wrapper\maven-wrapper.properties"
@SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar

@FOR /F "usebackq tokens=1,2 delims==" %%A IN (%MAVEN_WRAPPER_PROPERTIES%) DO (
    @IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@IF EXIST %MAVEN_WRAPPER_JAR% (
    @SET MVNW_VER=3
) ELSE (
    @ECHO Downloading Maven Wrapper...
    @powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '.mvn\wrapper\maven-wrapper.jar'}"
)

@IF "%MVNW_USERNAME%" == "" (
    @java %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath %MAVEN_WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%DP0%" org.apache.maven.wrapper.MavenWrapperMain %*
) ELSE (
    @java %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath %MAVEN_WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%DP0%" "-DMVNW_USERNAME=%MVNW_USERNAME%" "-DMVNW_PASSWORD=%MVNW_PASSWORD%" org.apache.maven.wrapper.MavenWrapperMain %*
)
