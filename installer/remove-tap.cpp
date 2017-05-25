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
	cout<<"Uninstalling SDXess..."<<endl;
	
	system("del files\\8VGVPN\\SDXess.bat");
	system("del files\\8VGVPN\\SDXess-debug.bat");
	system("del \"%HOMEDRIVE%%HOMEPATH%\\Desktop\\SDXess.lnk\"");
	system("del \"%HOMEDRIVE%%HOMEPATH%\\Desktop\\SDXess-debug.lnk\"");  
	system("files\\TAP-Windows\\tapinstall.exe remove tap0901");
	system("rmdir files /s /q");	
	cout<<"Done."<<endl;

    return EXIT_SUCCESS;
}






