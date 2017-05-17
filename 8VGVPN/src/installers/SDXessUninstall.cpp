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

int main(int argc, char *argv[])
{
	string location = ExePath();
	cout<<"Location: "<<location<<endl;
	cout<<"Uninstalling SDXess..."<<endl;
	
	system("del 8VGVPN\\SDXess.bat");
	system("del 8VGVPN\\SDXess-debug.bat");
	system("del \"%HOMEDRIVE%%HOMEPATH%\\Desktop\\SDXess.lnk\"");
	system("del \"%HOMEDRIVE%%HOMEPATH%\\Desktop\\SDXess-debug.lnk\"");  
	system("TAP-Windows\\tapinstall.exe remove tap0901");
	
	cout<<"Done."<<endl;
	
	system("pause");
    return EXIT_SUCCESS;
}






