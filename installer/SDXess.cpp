#include <windows.h>
#include <string>
	using namespace std;
	
	
string ExePath() {
    char buffer[MAX_PATH];
    GetModuleFileName( NULL, buffer, MAX_PATH );
    string::size_type pos = string( buffer ).find_last_of( "\\/" );
    return string( buffer ).substr( 0, pos);
}

int main(){
	string location = ExePath();
	string command = string("\"") + location + string("\\files\\8VGVPN\\SDXess.bat\"");
	system(  command.c_str() );
}
