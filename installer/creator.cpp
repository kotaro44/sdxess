#include <iostream>
#include <fstream>
#include <windows.h>
#include <string>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
	using namespace std;

ofstream fout;
void generateDATA();

BOOL Is64BitWindows()
{
	#if defined(_WIN64)
		return TRUE;  // 64-bit programs run only on Win64
	#elif defined(_WIN32)
		// 32-bit programs run on both 32-bit and 64-bit Windows
		// so must sniff
		BOOL f64 = FALSE;
		return IsWow64Process(GetCurrentProcess(), &f64) && f64;
	#else
		return FALSE; // Win64 does not support Win16
	#endif
}

void w(int n){
	fout.write(reinterpret_cast<const char *>(&n),1);
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

string ExePath() {
    char buffer[MAX_PATH];
    GetModuleFileName( NULL, buffer, MAX_PATH );
    string::size_type pos = string( buffer ).find_last_of( "\\/" );
    return string( buffer ).substr( 0, pos);
}

// requires DATA folder in same folder as this .exe
/*      DATA    from >> DATA.zip
    -8VGVPN
    	-build
    	-confs
    	-dist
    	-lib
    	-openvpn
    -TAP-Windows
    -Uninstall.exe
    -sdxess.ico

*/
void install(){
	if( Is64BitWindows() ){
		cout<<"64-Bit Mode"<<endl;
	}else{
		cout<<"32-Bit Mode"<<endl;
	}
	

	string location = ExePath();
	cout<<"Location: "<<location<<endl;
	cout<<"preparing to install SDXess..."<<endl;
    cout<<"generating SDXess.bat"<<endl;
	
	system("echo @echo off > files/8VGVPN/SDXess.bat");
	string root = location.substr(0,2);
	string command = "echo " + root + " >> files/8VGVPN/SDXess.bat";
	system( command.c_str() );
	command = string("echo cd \"") + location + string("\\files\\8VGVPN\" >> files/8VGVPN/SDXess.bat");
	system( command.c_str() );
	command = string("echo start javaw -jar dist/8VGVPN.jar >> files/8VGVPN/SDXess.bat");
	system( command.c_str() );
	
	cout<<"Done."<<endl<<endl;
	cout<<"generating SDXess-debug.bat"<<endl;
	
	command = "echo " + root + " > files/8VGVPN/SDXess-debug.bat";
	system( command.c_str() );
	command = string("echo cd \"") + location + string("\\files\\8VGVPN\" >> files/8VGVPN/SDXess-debug.bat");
	system(  command.c_str() );
	command = string("echo start java -jar dist/8VGVPN.jar >> files/8VGVPN/SDXess-debug.bat");
	system( command.c_str() );
	
	cout<<"Done."<<endl<<endl;
	cout<<"Creating desktop shortcuts!"<<endl;
	
	
	system("echo Set oWS = WScript.CreateObject(\"WScript.Shell\") > CreateShortcut.vbs");
	system("echo sLinkFile = \"\%HOMEDRIVE\%\%HOMEPATH\%\\Desktop\\SDXess.lnk\" >> CreateShortcut.vbs");
	system("echo Set oLink = oWS.CreateShortcut(sLinkFile) >> CreateShortcut.vbs");
	
	command = string("echo oLink.TargetPath = \"") + location + string("\\SDXess.exe\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	command = string("echo oLink.IconLocation = \"") + location + string("\\files\\sdxess.ico\" >> CreateShortcut.vbs");
	system(command.c_str());
	
	system("echo oLink.Save >> CreateShortcut.vbs");
	system("cscript CreateShortcut.vbs");
	system("del CreateShortcut.vbs");
	
	
	cout<<"Setting up TAP-Windows.."<<endl;
	system("files\\TAP-Windows\\tapinstall.exe remove tap0901");
	if( Is64BitWindows() ){
		system("start files\\TAP-Windows\\tapinstall.exe install files\\TAP-Windows\\driver\\OemVista.inf tap0901");
	}else{
		system("start files\\TAP-Windows\\32\\tapinstall.exe install files\\TAP-Windows\\32\\driver\\OemVista.inf tap0901");
	}
	
}

int main(){
	
	if( !IsElevated() ){
		cout<<"This Installer needs to be run as Administrador!"<<endl;
		system("pause");
		return 0;
	}
	
	struct stat info;

	install();
}
