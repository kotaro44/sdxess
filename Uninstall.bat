@echo off
cd %~dp0

echo Uninstalling SDXess...

del 8VGVPN\SDXess.bat
del 8VGVPN\SDXess-debug.bat
del "%HOMEDRIVE%%HOMEPATH%\Desktop\SDXess.lnk"
del "%HOMEDRIVE%%HOMEPATH%\Desktop\SDXess-debug.lnk"  
runas /user:Administrator "TAP-Windows\tapinstall.exe" remove tap0901

echo Done.

pause