
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
	jint (JNICALL *CreateJavaVM)(JavaVM **pvm, void **env, void *args);

	/* JDK 1.2 VM initialization arguments */ 
	JavaVMInitArgs vm_args; 

	char* class_path;
	char* library_path;

public:
	/**		find a jvm.dll either in a local directory, or in the registry	*/
	static char* find(StringList* local_path, StringList* preferred_version);


	/**		construct a JVM; the JVM is not launched, yet !		*/
	JVM();

	void setClassPath(char* class_path);

	void setLibraryPath(char* library_path);

	/**		launch the VM and call the "main" method	*/
	int launch(char* dll_path, int jni_version, StringList* options);

	int call(char* main_class, StringList* main_args);

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
	void setJavaVMOptions(int jni_version, StringList* args, char* class_path, char* library_path);

	jvalue* createMethodArgs(StringList* args);
};

#endif