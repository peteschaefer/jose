
#ifndef __JVM_DEFINED__
#define __JVM_DEFINED__

#include "strlist.h"

#include <jni.h>


class JVM
{

private:
	/* denotes a Java VM */ 
	JavaVM *jvm;       
	/* pointer to native method interface */ 
	JNIEnv *env;       
	/*		function pointer to JNI_CreateJavaVM */
//	typedef _JNI_IMPORT_OR_EXPORT_
//	jint JNICALL (*CreateJavaVMFunc) (JavaVM **pvm, void **env, void *args);
	typedef jint (*CreateJavaVMFunc) (JavaVM **pvm, void **env, void *args);
	typedef jint (*JNII_GetDefaultJavaVMInitArgsFunc)(void *args);

	/* JDK 1.2 VM initialization arguments */ 
	JavaVMInitArgs vm_args; 

	const char* class_path=nullptr;
	const char* library_path=nullptr;

public:
	/**		find a jvm.dll either in a local directory, or in the registry	*/
	static const char* find(const StringList* local_path, const StringList* preferred_version);


	/**		construct a JVM; the JVM is not launched, yet !		*/
	JVM();

	void setClassPath(const char* class_path);

	void setLibraryPath(const char* library_path);

	/**		launch the VM and call the "main" method	*/
	int launch(const char* dll_path, int jni_version, const StringList* options);

	int call(const char* main_class, const StringList* main_args);

	/**		destroy the VM */
	void destroy();


	/**	error constants	*/
	enum Errors {
		JVM_ERROR_DLL_MISSING						= -9,
		JVM_ERROR_DLL_NOT_FOUND						= -10,
		JVM_ERROR_CREATE_JAVA_VM_NOT_FOUND			= -11,
		JVM_ERROR_CREATE_JAVA_VM_FAILED				= -12,
		JVM_ERROR_MAIN_CLASS_NOT_FOUND				= -13,
		JVM_ERROR_MAIN_METHOD_NOT_FOUND				= -14,
		JVM_ERROR_BAD_MAIN_ARGS						= -15
	};

private:
	void setJavaVMOptions(int jni_version, const StringList* args, const char* class_path, const char* library_path);

	jvalue* createMethodArgs(const StringList* args);
};

#endif