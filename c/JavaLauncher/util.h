
#ifndef __UTIL_DEFINED__
#define __UTIL_DEFINED__


#ifdef _WINDOWS
#define DIRSEP '\\'
#else
#define DIRSEP '/'
#endif

bool existsFile(const char* file);

/**		string utility	*/
char* stringcat(const char* c1, const char* c2, const char* c3);

char* newString(char* str);

char* trim(char* str);

char* tolower(char* str);

char** unicodeToChar(unsigned short** unicode, int cnt);

char* unicodeToChar(unsigned short* unicode);

int unicodeStrlen(unsigned short* unicde);

char* replace(char* str, char a, char b);

//char* unescape(char* to, char* from);

#endif