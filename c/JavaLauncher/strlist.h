
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
	const char** data;
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
	StringList(const char* line);


	int size() const						{ return sz; }

	const char* get(int i) const			{ return data[i]; }

	int length(int i) const;

	const char* operator[] (int i) const	{ return data[i]; }

	const char* add(const char* str);

	const char* add(const char* str, int start, int len);

	int parse(const char* str);

	int parse1(const char* str);
	

protected:

	void ensureCapacity(int cap);

};

#endif