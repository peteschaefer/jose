
#include "winRegistry.h"
#include <jni.h>

#include <windows.h>
#include <exception>

extern void throwRuntimeException(JNIEnv* env, const char* pattern, ...);

#ifdef __cplusplus
extern "C" {
#endif

#define catch_all(env) \
	catch (std::exception& ex) { throwRuntimeException(env,"exception in native code: %s",ex.what()); } \
	catch (char* ex) { throwRuntimeException(env,"exception in native code: %s",ex); } \
	catch (int ex) { throwRuntimeException(env,"exception in native code: %i",ex); } \
	catch (...) { throwRuntimeException(env,"exception in native code"); }

void assertSuccess(int result, JNIEnv* env, char* message)
{
	if (result==ERROR_SUCCESS) return;

	LPSTR buffer;
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
		NULL, result, 0, (LPSTR)&buffer, 64, NULL);

	throwRuntimeException(env, "%s (%s, error code: %i)", message,buffer,result);
}

WCHAR* newChars(JNIEnv* env, jstring jstr)
{
	if (!jstr)
		return NULL;
	else {
		int len = env->GetStringLength(jstr);
		wchar_t* wchars = new wchar_t[len+1];
		env->GetStringRegion(jstr,0,len,(jchar*)wchars);
		wchars[len] = 0;	//	0-terminated
		return wchars;
	}
}

void freeChars(WCHAR* chars)
{
	if (chars) delete chars;
}

bool startsWith(WCHAR* str, char* prefix)
{
	while (*prefix)
		if (*str++ != *prefix++) return false;
	return true;
}

HKEY getRootKey(WCHAR* key)
{
	if (startsWith(key,"HKEY_CLASSES_ROOT\\")) return HKEY_CLASSES_ROOT;
	if (startsWith(key,"HKEY_CURRENT_CONFIG\\")) return HKEY_CURRENT_CONFIG;
	if (startsWith(key,"HKEY_CURRENT_USER\\")) return HKEY_CURRENT_USER;
	if (startsWith(key,"HKEY_LOCAL_MACHINE\\")) return HKEY_LOCAL_MACHINE;
	if (startsWith(key,"HKEY_PERFORMANCE_DATA\\")) return HKEY_PERFORMANCE_DATA;
	if (startsWith(key,"HKEY_PERFORMANCE_NLSTEXT\\")) return HKEY_PERFORMANCE_NLSTEXT;
	if (startsWith(key,"HKEY_PERFORMANCE_TEXT\\")) return HKEY_PERFORMANCE_TEXT;
	if (startsWith(key,"HKEY_USERS\\")) return HKEY_USERS;
	if (startsWith(key,"HKEY_DYN_DATA\\")) return HKEY_DYN_DATA;

	//	if all else fails:
	return NULL;
}

WCHAR* getSubKey(WCHAR* key)
{
	for (;;)
	{
		if (*key=='\\') return key+1;
		if (*key==0) return NULL;
		key++;
	}
}

int wstrlen(WCHAR* str)
{
	int i=0;
	while (*str++) i++;
	return i;
}

jobject NewInteger(JNIEnv* env, int32_t number)
{
	jclass cls = env->FindClass("java/lang/Integer");
	jmethodID midInit = env->GetMethodID(cls, "<init>", "(I)V");
	if (NULL == midInit) return NULL;
	jobject newObj = env->NewObject(cls, midInit, number);
    return newObj;
}

jobject NewLong(JNIEnv* env, int64_t number)
{
	jclass cls = env->FindClass("java/lang/Long");
	jmethodID midInit = env->GetMethodID(cls, "<init>", "(J)V");
	if (NULL == midInit) return NULL;
	jobject newObj = env->NewObject(cls, midInit, number);
	return newObj;
}

jobject toJavaObject(JNIEnv* env, BYTE* buffer, DWORD type, DWORD size)
{
	switch (type)
	{
    case REG_DWORD:
      	return NewInteger(env, *(int32_t*)buffer);
    case REG_QWORD:
		return NewLong(env, *(int64_t*)buffer);
	case REG_SZ:	//	0-terminated unicode string
		return env->NewString((jchar*)buffer, wstrlen((WCHAR*)buffer));
	default:
		//	other data types not yet supported
		//return NewLong(env,type);
		return env->NewStringUTF("not yet implemented");
	}
}



/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value
 * Signature: (Ljava/lang/String;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_de_jose_util_WinRegistry_get_1value__Ljava_lang_String_2I
  (JNIEnv* env, jclass, jstring jkey, jint index)
{
	jobject result = NULL;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;

		assertSuccess( RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");

		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		
		assertSuccess( RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL),
			env,"error retrieving key info");

		//	get value by index
		if (index >= 0 && (unsigned int)index < countValues)
		{
			char* valueName = new char[++maxValueNameLen];
			BYTE* buffer = new BYTE[++maxValueLen];
			DWORD type;
			DWORD size;
			assertSuccess( RegEnumValue(hkey,index, valueName,&maxValueNameLen, NULL, &type, buffer,&size),
				env,"could not read value");
			
			delete[] buffer;
			delete[] valueName;
		}

		RegCloseKey(hkey);

		freeChars(ckey);
	} catch_all(env)
	return result;
}


/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_de_jose_util_WinRegistry_get_1value__Ljava_lang_String_2Ljava_lang_String_2
  (JNIEnv* env, jclass, jstring jkey, jstring jvalue)
{
	jobject result = NULL;
	try {
		WCHAR* ckey = newChars(env,jkey);
		WCHAR* cvalue = newChars(env,jvalue);

		HKEY hkey;
		assertSuccess( RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");
		
		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		assertSuccess( RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL),
			env,"error retrieving key info");

		//	allocate result buffer
		BYTE* buffer = new BYTE[++maxValueLen];
		DWORD type;
		switch (RegQueryValueExW(hkey,cvalue,NULL,&type,buffer,&maxValueLen))
		{
		case ERROR_MORE_DATA:
			//	buffer too small ?
			throwRuntimeException(env,"buffer too small");
			break;
		case ERROR_SUCCESS:
		case 2:	//	why ? what is this ?
			result = toJavaObject(env, buffer,type,maxValueLen);
			break;
		default:
			throwRuntimeException(env,"unable to read value");
		}
		delete buffer;

		RegCloseKey(hkey);

		freeChars(ckey);
		freeChars(cvalue);
	} catch_all(env)
	return result;
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    set_string_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_jose_util_WinRegistry_set_1string_1value
  (JNIEnv* env, jclass, jstring jkey, jstring jvalue, jstring jdata)
{
	try {
		WCHAR* ckey = newChars(env,jkey);
		WCHAR* cvalue = newChars(env,jvalue);
		WCHAR* cdata = newChars(env,jdata);

		HKEY hkey;
		DWORD disposition;
		assertSuccess (RegCreateKeyExW(getRootKey(ckey),getSubKey(ckey),0,NULL,REG_OPTION_NON_VOLATILE,KEY_WRITE,NULL,&hkey,&disposition),
			env,"key not found");
		
		RegSetValueExW(hkey,cvalue,0,REG_SZ,(BYTE*)cdata, sizeof(WCHAR)*(wstrlen(cdata)+1));
		RegCloseKey(hkey);

		freeChars(ckey);
		freeChars(cvalue);
		freeChars(cdata);
	} catch_all(env)
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    exists_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_exists_1key
  (JNIEnv* env, jclass, jstring jkey)
{
	try {
		WCHAR* ckey = newChars(env,jkey);
		
		HKEY hkey;
		int result = RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_QUERY_VALUE,&hkey);
		if (result==ERROR_SUCCESS)
			RegCloseKey(hkey);

		freeChars(ckey);
		return result == ERROR_SUCCESS;
	} catch_all(env)
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    create_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_create_1key
  (JNIEnv* env, jclass, jstring jkey)
{
	DWORD disposition = 0;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;
		assertSuccess (RegCreateKeyExW(getRootKey(ckey),getSubKey(ckey),0,NULL,REG_OPTION_NON_VOLATILE,KEY_READ,NULL,&hkey,&disposition),
			env,"error creating key");

		RegCloseKey(hkey);

		
	} catch_all(env)
	
	return disposition==REG_CREATED_NEW_KEY;
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    delete_key
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_delete_1key
  (JNIEnv* env, jclass, jstring jkey)
{
	try {
		WCHAR* ckey = newChars(env,jkey);
		
		int result = RegDeleteKeyW(getRootKey(ckey),getSubKey(ckey));

		freeChars(ckey);
		return result == ERROR_SUCCESS;
	} catch_all(env)	
}


/*
 * Class:     de_jose_util_WinRegistry
 * Method:    delete_value
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinRegistry_delete_1value
  (JNIEnv* env, jclass, jstring jkey, jstring jvalue)
{
	try {
		WCHAR* ckey = newChars(env,jkey);
		WCHAR* cvalue = newChars(env,jvalue);

		HKEY hkey;
		int result = RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_WRITE,&hkey);
		if (result==ERROR_SUCCESS) 
		{
			result = RegDeleteValueW(hkey,cvalue);
			RegCloseKey(hkey);
		}


		freeChars(ckey);
		freeChars(cvalue);
		return result == ERROR_SUCCESS;
	} catch_all(env)
}


/*
 * Class:     de_jose_util_WinRegistry
 * Method:    count_subkeys
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinRegistry_count_1subkeys
  (JNIEnv* env, jclass, jstring jkey)
{
	int result;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;
		assertSuccess (RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");

		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		if (RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL) != ERROR_SUCCESS)
			throwRuntimeException(env,"error retrieving key info");

		result = countKeys;

		RegCloseKey(hkey);

		freeChars(ckey);
	} catch_all(env)

	return result;
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    count_values
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinRegistry_count_1values
  (JNIEnv* env, jclass, jstring jkey)
{
	int result;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;
		assertSuccess (RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");

		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		assertSuccess (RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL),
			env,"error retrieving key info");

		result = countValues;

		RegCloseKey(hkey);

		freeChars(ckey);
	} catch_all(env)

	return result;
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_value_name
 * Signature: (Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_jose_util_WinRegistry_get_1value_1name
  (JNIEnv* env, jclass, jstring jkey, jint index)
{
	jstring result = NULL;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;

		assertSuccess (RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");

		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		assertSuccess (RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL),
			env,"error retrieving key info");

		//	get value by index
		char* valueName = new char[++maxValueNameLen];
		switch (RegEnumValue(hkey,index, valueName,&maxValueNameLen, NULL,NULL,NULL,NULL))
		{
		case ERROR_MORE_DATA:
			//	buffer too small
			throwRuntimeException(env,"buffer too small");
			break;
		case ERROR_SUCCESS:
			valueName[maxValueNameLen]=0;
			result = env->NewStringUTF(valueName);
			break;
		default:
			throwRuntimeException(env,"could not read value");
		}
		
		delete valueName;

		RegCloseKey(hkey);

		freeChars(ckey);
	} catch_all(env)
	return result;	
}

/*
 * Class:     de_jose_util_WinRegistry
 * Method:    get_subkey
 * Signature: (Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_jose_util_WinRegistry_get_1subkey
  (JNIEnv* env, jclass, jstring jkey, jint index)
{
	jstring result = NULL;
	try {
		WCHAR* ckey = newChars(env,jkey);

		HKEY hkey;

		assertSuccess (RegOpenKeyExW(getRootKey(ckey),getSubKey(ckey),0,KEY_READ,&hkey),
			env,"key not found");

		//	determine required buffer sizes
		DWORD countKeys;
		DWORD maxKeyLen;
		DWORD countValues;
		DWORD maxValueNameLen;
		DWORD maxValueLen;
		assertSuccess (RegQueryInfoKey(hkey, NULL,NULL, NULL,&countKeys,&maxKeyLen, NULL, 
				&countValues,&maxValueNameLen,&maxValueLen,NULL,NULL),
			env,"error retrieving key info");

		//	get key name by index
		if (index >= 0 && (unsigned int)index < countKeys)
		{
			char* buffer = new char[++maxKeyLen];
			FILETIME lastmod;
			assertSuccess (RegEnumKeyEx(hkey,index, buffer,&maxKeyLen, NULL,NULL,NULL,&lastmod),
				env,"could not retrieve key");

			buffer[maxKeyLen] = 0;
			result = env->NewStringUTF(buffer);

			delete buffer;
		}
		RegCloseKey(hkey);

		freeChars(ckey);
	} catch_all(env)
	return result;
}

#ifdef __cplusplus
}
#endif
