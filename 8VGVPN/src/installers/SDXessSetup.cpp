#include <windows.h>
#include <cstdlib>
#include <iostream>
#include <string>

using namespace std;


string ExePath() {
    char buffer[MAX_PATH];
    GetModuleFileName( NULL, buffer, MAX_PATH );
    string::size_type pos = string( buffer ).find_last_of( "\\/" );
    return string( buffer ).substr( 0, pos);
}

BOOL IsElevated( ) {
    BOOL fRet = FALSE;
    HANDLE hToken = NULL;
    if( OpenProcessToken( GetCurrentProcess( ),TOKEN_QUERY,&hToken ) ) {
        TOKEN_ELEVATION Elevation;
        DWORD cbSize = sizeof( TOKEN_ELEVATION );
        if( GetTokenInformation( hToken, TokenElevation, &Elevation, sizeof( Elevation ), &cbSize ) ) {
            fRet = Elevation.TokenIsElevated;
        }
    }
    if( hToken ) {
        CloseHandle( hToken );
    }
    return fRet;
}

int main(int argc, char *argv[])
{
	
	if( !IsElevated() ){
		cout<<"This Installer needs to be run as Administrador!"<<endl;
		system("pause");
		return 0;
	}
	
	
	string location = ExePath();
	cout<<"Location: "<<location<<endl;
	cout<<"preparing to install SDXess..."<<endl;
    cout<<"generating SDXess.bat"<<endl;
	
	system("echo @echo off > 8VGVPN/SDXess.bat");
	string command = string("echo cd \"") + location + string("\\8VGVPN\" >> 8VGVPN/SDXess.bat");
	system(  command.c_str() );
	command = string("echo start javaw -jar dist/8VGVPN.jar >> 8VGVPN/SDXess.bat");
	system(  command.c_str() );
	
	cout<<"Done."<<endl<<endl;
	cout<<"generating SDXess-debug.bat"<<endl;
	
	command = string("echo cd \"") + location + string("\\8VGVPN\" > 8VGVPN/SDXess-debug.bat");
	system(  command.c_str() );
	command = string("echo start java -jar dist/8VGVPN.jar >> 8VGVPN/SDXess-debug.bat");
	system( command.c_str() );
	
	cout<<"Done."<<endl<<endl;
	cout<<"Creating desktop shortcuts!"<<endl;
	
	
	system("echo Set oWS = WScript.CreateObject(\"WScript.Shell\") > CreateShortcut.vbs");
	system("echo sLinkFile = \"\%HOMEDRIVE\%\%HOMEPATH\%\\Desktop\\SDXess.lnk\" >> CreateShortcut.vbs");
	system("echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs");
	
	command = string("echo oLink.TargetPath = \"") + location + string("\\8VGVPN\\SDXess.bat\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	command = string("echo oLink.IconLocation = \"") + location + string("\\sdxess.ico\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	system("echo oLink.Save >> CreateShortcut.vbs");
	system("cscript CreateShortcut.vbs");
	system("del CreateShortcut.vbs");
	system("echo Set oWS = WScript.CreateObject(\"WScript.Shell\") > CreateShortcut.vbs");
	system("echo sLinkFile = \"\%HOMEDRIVE\%\%HOMEPATH\%\\Desktop\\SDXess-debug.lnk\" >> CreateShortcut.vbs");
	system("echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs");
	
	command = string("echo oLink.TargetPath = \"") + location + string("\\8VGVPN\\SDXess-debug.bat\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	command = string("echo oLink.IconLocation = \"") + location + string("\\sdxess.ico\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	system("echo oLink.Save >> CreateShortcut.vbs");
	system("cscript CreateShortcut.vbs");
	system("del CreateShortcut.vbs");
	
	
	cout<<"Setting up TAP-Windows.."<<endl;
	system("TAP-Windows\\tapinstall.exe install TAP-Windows\\driver\\OemVista.inf tap0901");

	system("pause");
    return EXIT_SUCCESS;
}









