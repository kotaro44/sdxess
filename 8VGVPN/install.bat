@echo off
set current=%cd%

echo preparing to install SDXess...
echo generating SDXess.bat

echo @echo off > SDXess.bat
echo cd "%current%" >> SDXess.bat
echo start javaw -jar dist/8VGVPN.jar >> SDXess.bat

echo Done.
echo generating SDXess-debug.bat

echo cd "%current%" > SDXess-debug.bat
echo java -jar dist/8VGVPN.jar >> SDXess-debug.bat

echo Done.

echo SDXess.bat Generated! (To start: Run SDXess.bat as Administrator)

pause