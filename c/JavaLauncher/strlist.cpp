
#include "strlist.h"

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

/**
 * ctor from arg list
 */
StringList::StringList(int argc, char** argv)
{
	sz = capacity = argc;
	data = argv;
	alloced = false;
}

StringList::StringList()
{
	sz = capacity = 0;
	data = NULL;
	alloced = false;
}

StringList::StringList(char* line)
{
	sz = capacity = 0;
	data = NULL;
	alloced = false;
	parse(line);
}

char* StringList::add(char* str) 
{
	ensureCapacity(sz+1);
	return (data[sz++] = str);
}

char* StringList::add(char* str, int start, int len)
{
	//		copy
	char* newstr = (char*)malloc(len+1);
	memcpy(newstr,str+start, len);
	newstr[len] = 0;
	return add(newstr);
}

void StringList::ensureCapacity(int cap)
{
	if (cap <= capacity) return;

	int newCapacity = (capacity<=0) ? 4:2*capacity;
	while (newCapacity < cap) newCapacity *= 2;

	//		create new data
	char** newData = (char**)malloc(newCapacity * sizeof(char*));
		
	if (data != NULL) {
		//		copy
		memcpy(newData,data, sz*sizeof(char*));
		//		free old data
		if (alloced) free(data);
	}

	data = newData;
	capacity = newCapacity;
	alloced = true;
}


#define WHITE			1
#define TEXT			2
#define QUOTED_TEXT		3

#define START_TOKEN(x,newstate)		{ token=x; state = newstate; }
#define END_TOKEN(x,newstate)		{ if (x>token) add(token,0,x-token); state = newstate; }


int StringList::parse(char* line)
{
	char* s = line;
	char* token = NULL;
	int size_before = size();

	short state = WHITE;

	for(;; s++)
		switch (*s)
		{
		case 0:	if (state!=WHITE) END_TOKEN(s,0)
				return size()-size_before;

		case ' ':
		case '\t':
		case '\n':		//		white space
				if (state==TEXT) END_TOKEN(s,WHITE)
				continue;

		case '"':		//		quote
				switch (state) {
				case WHITE:			START_TOKEN(s+1,QUOTED_TEXT) continue;
				case QUOTED_TEXT:	END_TOKEN(s,WHITE) continue;
				}
				break;

		case '\\':		//		DON'T escape
//				strcpy(s,s+1);
				//		fall through intended
		default:		//		text char
				if (state==WHITE) START_TOKEN(s,TEXT) 
				continue;
		}
	
}

int StringList::length(int i)
{ 
	return strlen(get(i)); 
}

int StringList::parse1(char* line)
{
	char* s = line;
	char* token = NULL;

	short state = WHITE;

	for(;; s++)
		switch (*s)
		{
		case 0:	if (state!=WHITE) {
					END_TOKEN(s,0)
					return 1;
				}
				else
					return 0;

		case ' ':
		case '\t':
		case '\n':		//		white space
				if (state==TEXT) {
					END_TOKEN(s,WHITE)
					return 1;
				}
				continue;

		case '"':		//		quote
				switch (state) {
				case WHITE:			START_TOKEN(s+1,QUOTED_TEXT) continue;
				case QUOTED_TEXT:	END_TOKEN(s,WHITE) return 1;
				}
				break;

		case '\\':		//	DON'T	escape
//				strcpy(s,s+1);
				//		fall through intended
		default:		//		text char
				if (state==WHITE) START_TOKEN(s,TEXT) 
				continue;
		}
	
	return 0;
}