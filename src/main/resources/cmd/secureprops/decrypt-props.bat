



REM Sets the path to the SecureProps tool home folder example c:\Mule\secureprops
set securepropsHome = { set home folder for secureprops tool }

REM  Sets the secure key for the environment (e.g., dummyPwd123LOCAL)
set keyForLocal = { set secure key for local }
set keyForDev   = { set secure key for dev }
set keyForProd  = { set secure key for prod }



set mule-secureprops-extension=%securepropsHome%\mule-secureprops-extension-1.0-SNAPSHOT.jar 

set envKeyMapping="(secure-config-local.yaml):(%keyForLocal%),(secure-config-(dev|uat|test|mock).yaml):(%keyForDev%),(secure-config-prod.yaml):(%keyForProd%)"


echo %envKeyMapping%

@echo off
REM Use current directory (.) if no argument is passed
if "%~1"=="" (
    set "directory=."
) else (
    set "directory=%~1"
)

java -jar %mule-secureprops-extension% decrypt file %directory% AES CBC false --envKeyMapping=%envKeyMapping% --tmp=.




