#include "winUtils.h"
#include <windows.h>
#include <exception>
//#include <shfolder.h>
#include <shlobj.h>
#include <jni.h>

jclass class_RuntimeException;
char* error_message = NULL;

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv* env;
	if (vm->GetEnv((void**)&env,JNI_VERSION_1_4)==JNI_OK)
	{
		class_RuntimeException = env->FindClass("java/lang/RuntimeException");
		class_RuntimeException = (jclass)env->NewGlobalRef(class_RuntimeException);
	}
	return JNI_VERSION_1_4;
}

void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
	//	nothing to clean up	
}

void throwRuntimeException(JNIEnv* env, const char* pattern, ...)
{
	if (!error_message) error_message = new char[1024];
	va_list varargs;
	va_start(varargs,pattern);

	vsprintf(error_message,pattern,varargs);

	va_end(varargs);

	env->ThrowNew(class_RuntimeException,error_message);
}

#define catch_all(env) \
	catch (std::exception& ex) { throwRuntimeException(env,"exception in native code: %s",ex.what()); } \
	catch (char* ex) { throwRuntimeException(env,"exception in native code: %s",ex); } \
	catch (int ex) { throwRuntimeException(env,"exception in native code: %i",ex); } \
	catch (...) { throwRuntimeException(env,"exception in native code"); }


/* Inaccessible static: libLoaded */
/*
 * Class:     de_jose_util_WinUtils
 * Method:    getPriorityClass
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinUtils_getPriorityClass
  (JNIEnv* env, jclass, jlong procHnd)
{
	try {
		return GetPriorityClass((HANDLE)procHnd);
	} catch_all(env)
}


/*
 * Class:     de_jose_util_WinUtils
 * Method:    setPriorityClass
 * Signature: (JI)Z
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinUtils_setPriorityClass
  (JNIEnv* env, jclass, jlong procHnd, jint priority)
{
	try {
		return SetPriorityClass((HANDLE)procHnd,priority) != 0;
	} catch_all(env)
}

/*
 * Class:     de_jose_util_WinUtils
 * Method:    windowToFront
 * Signature: (J)V
 */
JNIEXPORT jboolean JNICALL Java_de_jose_util_WinUtils_setTopMost
  (JNIEnv* env, jclass, jlong winHnd)
{
	try {
		HWND hwnd = (HWND)winHnd;
		return SetWindowPos(hwnd, HWND_TOPMOST, 0,0,0,0,
			SWP_NOSIZE | SWP_NOMOVE) != 0;
	} catch_all(env)
}

/*
 * Class:     de_jose_util_WinUtils
 * Method:    findWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_jose_util_WinUtils_findWindow
  (JNIEnv* env, jclass, jstring title)
{
	try {
		HWND hwnd = NULL;
		const char *str = NULL;

		str = env->GetStringUTFChars(title, NULL);
		hwnd = FindWindowA(NULL,str);
		
		env->ReleaseStringUTFChars(title, str);
		return (jlong) hwnd;
	} catch_all(env)
}


JNIEXPORT jstring JNICALL Java_de_jose_util_WinUtils_getWindowsDirectory
	(JNIEnv* env, jclass)
{
	try {
		char fontChars[MAX_PATH];

		GetWindowsDirectoryA(fontChars,MAX_PATH);

		return env->NewStringUTF((const char*)fontChars);
	} catch_all(env)
}


JNIEXPORT jstring JNICALL Java_de_jose_util_WinUtils_getSystemDirectory
	(JNIEnv* env, jclass, jint folder)
{
	try {
		char fontChars[MAX_PATH];

		//	note: this method might not work with Windows 98
	//	SHGetFolderPathA(NULL, folder, NULL, 0, fontChars);

		SHGetSpecialFolderPath(NULL, fontChars, folder, false);

		return env->NewStringUTF((const char*)fontChars);
	} catch_all(env)
}