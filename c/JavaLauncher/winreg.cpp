
#include <stdlib.h>
#include <string.h>
#include <windows.h>

#include "winreg.h"
#include "util.h"


char* getRegistryValue(const char* akey, const char *version, const char* subkey)
{
	HKEY hkey;
    DWORD size;
    DWORD type;

	char* key = stringcat(akey,"\\",version);

	int errcode = RegOpenKeyEx(HKEY_LOCAL_MACHINE, key, 0,KEY_READ, &hkey);
	if (errcode != ERROR_SUCCESS) {
		free(key);
		return NULL;
	}	
	free(key);
	
	errcode = RegQueryValueEx(hkey,subkey,NULL,&type,NULL,&size);
	if (type!=REG_SZ) {
		RegCloseKey(hkey);
		return NULL;
	}
    if (errcode != ERROR_SUCCESS) {
		RegCloseKey(hkey);
        return NULL;
	}
	
	char* result = (char*)malloc(size);
	RegQueryValueEx(hkey,subkey,NULL,NULL,(unsigned char*)result,&size);        
	RegCloseKey(hkey);
	return result;
}


char* enumRegistryKey(const char* key, int idx)
{
	HKEY hkey;
    
	if (RegOpenKey(HKEY_LOCAL_MACHINE, key, &hkey) != ERROR_SUCCESS) 
        return NULL;

	char* result = (char*)malloc(64);
    if (RegEnumKey(hkey,idx,result,64) != ERROR_SUCCESS) {
		free(result);
		return NULL;
	}
	else
		return result;
}
