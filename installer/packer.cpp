#include <iostream>
#include <stdlib.h>
#include <windows.h>
#include <string>
	using namespace std;
	
bool dirExists(const std::string& dirName_in)
{
  DWORD ftyp = GetFileAttributesA(dirName_in.c_str());
  if (ftyp == INVALID_FILE_ATTRIBUTES)
    return false;  //something is wrong with your path!

  if (ftyp & FILE_ATTRIBUTE_DIRECTORY)
    return true;   // this is a directory!

  return false;    // this is not a directory!
}

int main()
{
	system("del DATA");
	
	cout<< "creating DATA directory..."<<endl;
    system("mkdir DATA");
    
    cout<<"copying TAP-Windows folder..."<<endl;
    system("mkdir DATA\\TAP-Windows");
    system("xcopy ..\\TAP-Windows DATA\\TAP-Windows /s /e /h");
	
	system("copy Uninstall.exe DATA\\Uninstall.exe");
	system("copy sdxess.ico DATA\\sdxess.ico");
	
	cout<<"creating 8VGVPN folder..."<<endl;
	system("mkdir DATA\\8VGVPN");
	
	system("mkdir DATA\\8VGVPN\\confs");
	system("xcopy ..\\8VGVPN\\confs DATA\\8VGVPN\\confs /s /e /h");
	
	system("mkdir DATA\\8VGVPN\\dist");
	system("xcopy ..\\8VGVPN\\dist DATA\\8VGVPN\\dist /s /e /h");
	
	system("mkdir DATA\\8VGVPN\\lib");
	system("xcopy ..\\8VGVPN\\lib DATA\\8VGVPN\\lib /s /e /h");
	
	system("mkdir DATA\\8VGVPN\\openvpn");
	system("xcopy ..\\8VGVPN\\openvpn DATA\\8VGVPN\\openvpn /s /e /h");
	
	system("powershell.exe -nologo -noprofile -command \"& { Add-Type -A 'System.IO.Compression.FileSystem'; [IO.Compression.ZipFile]::CreateFromDirectory('DATA', 'TEMP'); }\"");
	
	system("rmdir DATA /s /q");
	system("rename TEMP DATA");
	
	system("pause");
}
