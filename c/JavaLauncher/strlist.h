
#ifndef __STRLIST_DEFINED__
#define __STRLIST_DEFINED__


class StringList
{
private:
	/**	current size	*/
	int sz;
	/**	current capacity		*/
	int capacity;
	/** string data */
	char** data;
	/** was data allocated by this class ? */
	bool alloced;

public:

	/**
	 * ctor from arg list
	 */
	StringList(int argc, char** argv);

	/**
	 * empty ctor
	 */
	StringList();

	/**
	 */
	StringList(char* line);


	int size()						{ return sz; }

	char* get(int i)				{ return data[i]; }

	int length(int i);

	char* operator[] (int i)		{ return data[i]; }

	char* add(char* str);

	char* add(char* str, int start, int len);

	int parse(char* str);

	int parse1(char* str);
	

protected:

	void ensureCapacity(int cap);

};

#endif