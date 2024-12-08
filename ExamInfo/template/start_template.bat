@echo off
set JAVA_HOME=.\jre16
set PATH=%JAVA_HOME%\bin;%PATH%
if "%1" == "h" goto begin
mshta vbscript:createobject("wscript.shell").run("""%~nx0"" h",0)(window.close)&&exit
:begin
java -Djavax.net.ssl.trustStore=.\cert\mycerts.jks -Djavax.net.ssl.trustStorePassword=changeit -jar ExamInfo-${project.version}.jar