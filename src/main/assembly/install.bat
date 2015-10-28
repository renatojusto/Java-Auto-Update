@echo off

:: Remove very old agent if present
if exist C:\NMD\avbagent\pharmacy-agent\bin\pharmacy-agent (
	C:\NMD\avbagent\pharmacy-agent\bin\pharmacy-agent remove
	taskkill /im java.exe /F
)

:: If previous version of java-auto-update and agent is running remove them
if exist c:\opt\java-auto-update\bin\java-auto-update (
	c:\opt\java-auto-update\bin\java-auto-update remove
	taskkill /im java.exe /F
)


:: Download java if java folder is missing
if not exist java (
    call C:\opt\java-auto-update\download-java.bat
)

bin\java-auto-update remove & bin\java-auto-update install & bin\java-auto-update start & sc failure java-auto-update reset= 500000  actions= restart/30000/restart/90000/restart/300000
