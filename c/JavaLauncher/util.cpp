
#include "util.h"

#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <cstdio>

#ifdef _WINDOWS
#include <windows.h>

bool existsFile(const char* file)
{
	WIN32_FIND_DATA find_data;
	HANDLE hFind = FindFirstFile(file,&find_data);

	if (FindFirstFile(file,&find_data) != INVALID_HANDLE_VALUE)
	{
		FindClose(hFind);
		return 1;
	}
	return 0;
}
#else
bool existsFile(const char* file)
{
	FILE* f = fopen(file,"r");
	if (f) {
		fclose(f);
		return true;
	}
	return false;
}
#endif



char* stringcat(const char* c1, const char* c2, const char* c3)
{
	int l1 = 0;
	int l2 = 0;
	int l3 = 0;

	if (c1 != NULL) l1 = strlen(c1);
	if (c2 != NULL) l2 = strlen(c2);
	if (c3 != NULL) l3 = strlen(c3);

	int len = l1+l2+l3;

	char* result = (char*)malloc(len+1);
	char* end = result;
	if (l1 > 0) {
		memcpy(end,c1,l1);
		end += l1;
	}
	if (l2 > 0) {
		memcpy(end,c2,l2);
		end += l2;
	}
	if (l3 > 0) {
		memcpy(end,c3,l3);
		end += l3;
	}
	*end = '\0';
	return result;
}

char* newString(char* str)
{
	int len = strlen(str);
	char* result = (char*)malloc(len+1);
	memcpy(result,str,len+1);
	return result;
}

int unicodeStrlen(unsigned short* unicode)
{
	int len = 0;
	while (*unicode++) len++;
	return len;
}

char** unicodeToChar(unsigned short** unicode, int cnt)
{
	char** result = (char**)calloc(cnt,sizeof(char*));
	for (int i=0; i<cnt; i++)
		result[i] = unicodeToChar(unicode[i]);
	return result;
}

char* unicodeToChar(unsigned short* unicode)
{
	int len = unicodeStrlen(unicode);
	char* result = (char*)malloc(len+1);

	char* s = result;
	while (len--) *s++ = (char)*unicode++;
	*s = 0;

	return result;
}

char* trim(char* str)
{
	//	trim leading whitespace
	while (*str) {
		switch (*str) {
		case ' ':
		case '\t':
		case '\n':		str++; continue;
		}
		break;
	}

	//	trim trainling whitespace
	char* end = str+strlen(str);
	while (end > str) {
		switch (end[-1]) {
		case ' ':
		case '\t':
		case '\n':		end--; continue;
		}
		break;
	}

	*end = 0;
	return str;
}

char* tolower(char* str)
{
	char* s = str;
	while (*s) {
		*s = tolower(*s);
		s++;
	}
	return str;
}

char* replace(char* str, char a, char b)
{
	char* s = str;
	while (*s) 
	{
		if (*s==a) *s = b;
		s++;
	}
	return str;
}
