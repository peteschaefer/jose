
#include <windows.h>

#include "jvm.h"
#include "winreg.h"
#include "util.h"


/**		construct a JVM; the JVM is not launched, yet !		*/
JVM::JVM()
{ }

void JVM::setClassPath(char* path)
{
	class_path = path;
}

void JVM::setLibraryPath(char* path)
{
	library_path = path;
}

void JVM::setJavaVMOptions(int jni_version, 
						StringList* args, 
						char* class_path, 
						char* library_path)
{
	vm_args.nOptions = args->size();
	vm_args.options = new JavaVMOption[vm_args.nOptions+2];

	for (int i=0; i<vm_args.nOptions; i++)
		vm_args.options[i].optionString = args->get(i);

	if (class_path != NULL)
		vm_args.options[vm_args.nOptions++].optionString = stringcat("-Djava.class.path=",class_path,NULL);
	if (library_path != NULL)
		vm_args.options[vm_args.nOptions++].optionString = stringcat("-Djava.library.path=",library_path,NULL);

	vm_args.version = jni_version; /* JDK 1.4 is required */ 
	vm_args.ignoreUnrecognized = JNI_FALSE;
}


/**		launch the VM and call the "main" method	*/
int JVM::launch(char* dll_path, int jni_version, StringList* jvm_options)
{
	if (dll_path==NULL)
		return JVM_ERROR_DLL_MISSING;

	/* 
	 * load jvm.dll	 
	 */	
	HINSTANCE dll_handle = LoadLibrary(dll_path);
	if (dll_handle==NULL)
		return JVM_ERROR_DLL_NOT_FOUND;
		
	/* setup JVM arguments
	 */ 
	setJavaVMOptions(jni_version,jvm_options, class_path,library_path);

	/*	launch VM; call method from jvm.dll
	 */
	CreateJavaVM = (jint (JNICALL *)(JavaVM **,void **, void *))
						GetProcAddress(dll_handle, "JNI_CreateJavaVM");
	if (CreateJavaVM==NULL)
		return JVM_ERROR_CREATE_JAVA_VM_NOT_FOUND;

	int res = CreateJavaVM(&jvm, (void**)&env, &vm_args);
	if (res < 0)
		return JVM_ERROR_CREATE_JAVA_VM_FAILED;
	return +1;
}

jvalue* JVM::createMethodArgs(StringList* args)
{
	jclass stringClass = env->FindClass("java/lang/String");
	jobjectArray array = env->NewObjectArray(args->size(),stringClass, NULL);
	if (array==NULL) return NULL;

	for (int i=0; i < args->size(); i++) {
		env->SetObjectArrayElement(array,i,  env->NewStringUTF(args->get(i)));
	}

	jvalue* value = new jvalue();
	value->l = array;
	return value;
}

int JVM::call(char* main_class, StringList* args)
{
	/* 
	 * invoke the main method using JNI 
	 */ 
	jclass cls = env->FindClass(main_class); 
	if (cls==NULL)
		return JVM_ERROR_MAIN_CLASS_NOT_FOUND;

	jmethodID mid = env->GetStaticMethodID(cls, "main", "([Ljava/lang/String;)V"); 
	if (mid==0)
		return JVM_ERROR_MAIN_METHOD_NOT_FOUND;
			
	/*
	 * setup arguments for main(String[] args)
	 */	
	jvalue* value = createMethodArgs(args);
	if (value==NULL)
		return JVM_ERROR_BAD_MAIN_ARGS;

	/* call main() !*/
	env->CallStaticVoidMethod(cls, mid, *value); 
	return 0;
}


/**		destroy the VM */
void JVM::destroy()
{
	jvm->DestroyJavaVM(); 		
}



//	registry key for JRE
char* JRE_KEY			= "Software\\JavaSoft\\Java Runtime Environment";
//	registry key for JDK
char* JDK_KEY			= "Software\\JavaSoft\\Java Development Kit";
//	registry key for Plug-in
char* PLUGIN_KEY		= "Software\\JavaSoft\\Java Plug-in";

char* findJvmInRegistry(char* version)
{
	char* jvm;
	char* javahome;

	/**	look for JRE/RuntimeLib	*/
	jvm = getRegistryValue(JRE_KEY,version, "RuntimeLib");
	if (jvm!=NULL) {
		if (existsFile(jvm))
			return jvm;
		else
			free(jvm);
	}

	/**	look for JRE/JavaHome	*/
	javahome = getRegistryValue(JRE_KEY,version, "JavaHome");
	if (javahome!=NULL) {
		jvm = stringcat(javahome,"\\bin\\client\\jvm.dll",NULL);
		free(javahome);

		if (existsFile(jvm))
			return jvm;
		else
			free(jvm);
	}

	/**	look for JDK/JavaHome	*/
	javahome = getRegistryValue(JDK_KEY,version, "JavaHome");
	if (javahome!=NULL) {
		jvm = stringcat(javahome,"\\jre\\bin\\client\\jvm.dll",NULL);
		free(javahome);

		if (existsFile(jvm))
			return jvm;
		else
			free(jvm);
	}

	/**	look for Plug-in/JavaHome	*/
	javahome = getRegistryValue(PLUGIN_KEY,version, "JavaHome");
	if (javahome!=NULL) {
		jvm = stringcat(javahome,"\\jre\\bin\\client\\jvm.dll",NULL);
		free(javahome);

		if (existsFile(jvm))
			return jvm;
		else
			free(jvm);
	}

	return NULL;
}


char* findJvmInRegistry(char* key, char* preferred_version)
{
	for (int i=0; ; i++)
	{
		char* version = enumRegistryKey(key,i);
		if (version==NULL) 
			break;
		if (preferred_version==NULL || strcmp(version,preferred_version) >= 0) 
		{
			char* jvm = findJvmInRegistry(version);
			if (jvm!=NULL) {
				free(version);
				return jvm;
			}			
		}
		free(version);
	}
	return NULL;
}

		


/**		find a jvm.dll either in a local directory, or in the registry	*/
char* JVM::find(StringList* local_path, StringList* preferred_version)
{
	/* 1. look for bundled JRE in working directory */
	for (int j=0; j<local_path->size(); j++)
		if (existsFile(local_path->get(j)))
			return local_path->get(j);

	/* 2. look for preferred version in registry */
	char* jvm;
	for (j=0; j<preferred_version->size(); j++)
	{
			/**	try current 1.4	*/
			char* pref = preferred_version->get(j);
			jvm = findJvmInRegistry(pref);
			if (jvm!=NULL) return jvm;
	}

	for (j=0; j<preferred_version->size(); j++)
	{
			/**	try any 1.4	*/
			char* pref = preferred_version->get(j);
			jvm = findJvmInRegistry(JRE_KEY,pref);
			if (jvm!=NULL) return jvm;

			jvm = findJvmInRegistry(JDK_KEY,pref);
			if (jvm!=NULL) return jvm;

			jvm = findJvmInRegistry(PLUGIN_KEY,pref);
			if (jvm!=NULL) return jvm;
	}

	/**	try older JVMs	*/
	jvm = findJvmInRegistry(JRE_KEY,NULL);
	if (jvm!=NULL) return jvm;

	jvm = findJvmInRegistry(JDK_KEY,NULL);
	if (jvm!=NULL) return jvm;

	jvm = findJvmInRegistry(PLUGIN_KEY,NULL);
	if (jvm!=NULL) return jvm;
	
	//	all fails
	return NULL;
}



