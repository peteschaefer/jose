
#ifndef __WINREG_DEFINED__
#define __WINREG_DEFINED__

/**		get a subkey	*/
char* enumRegistryKey(const char* key, int idx);

/**		get a value		*/
char* getRegistryValue(const char* akey, const char *version, const char* subkey);


#endif