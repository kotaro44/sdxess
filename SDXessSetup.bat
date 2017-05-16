@echo off
cd 8VGVPN
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
echo Creating shortcut!

echo Set oWS = WScript.CreateObject("WScript.Shell") > CreateShortcut.vbs
echo sLinkFile = "%HOMEDRIVE%%HOMEPATH%\Desktop\SDXess.lnk" >> CreateShortcut.vbs
echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs
echo oLink.TargetPath = "%current%\SDXess.bat" >> CreateShortcut.vbs
echo oLink.Save >> CreateShortcut.vbs
cscript CreateShortcut.vbs
del CreateShortcut.vbs

echo Done. Run SDXess as Administrator.

pause