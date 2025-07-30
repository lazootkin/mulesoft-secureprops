



REM Sets the path to the SecureProps tool home folder example c:\Mule\secureprops
set securepropsHome = { set home folder for secureprops tool }
 
 
set mule-secureprops-extension=%securepropsHome%\mule-secureprops-extension-1.0-SNAPSHOT.jar 

  
java -jar %mule-secureprops-extension% --ui

pause


 







