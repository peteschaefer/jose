//
// Created by nightrider on 25.01.2025.
//

#include <cstdlib>
#include <cstring>

#include <jni.h>

#include "jvm.h"
#include "jlaunch.h"
#include "util.h"

#define MISSING_JRE 	"Java Runtime Environment not found.\n" \
						"Please install one (preferrably version 21)."


const char* getIniFile(const char* arg0)
{
    //		trim trailing ".exe"
    size_t len = strlen(arg0);
    if (len > 4 && strcmp(arg0+len-4,".exe")==0)
        ((char*)arg0)[len-4] = 0;
    else if (len > 4 && strcmp(arg0+len-4,".EXE")==0)
        ((char*)arg0)[len-4] = 0;
    return stringcat(arg0,".ini",NULL);
}

const char* getWorkDir(const char* arg0)
{
    const char* s = arg0+strlen(arg0);
    while (s > arg0 && s[-1] != DIRSEP)
        s--;
    ((char*)s)[0] = 0;
    return arg0;
}


StringList splash;

int launch(StringList* argv)
{
	/** used to launch the JVM */
	JVM* jvm = new JVM();

	/** main class entry point	*/
	char* main_class = NULL;
	/** arguments to main() method */
	StringList main_args;

	/** path to look for bundled JVMs */
	StringList local_jvm_path;
	/** JVM command line options */
	StringList jvm_options;

	/** preferred versions in registry */
	StringList preferred_version;

	/*
	 *	parse ini file
	 */
	const char* ini_file = getIniFile(argv->get(0));

	if (ini_file != NULL) {
			FILE* file = fopen(ini_file,"r");
			if (file==NULL)
				fatal("could not read .ini file",0);

			char* line = new char[256];

			while (fgets(line,256,file)!=NULL)
			{
				if (line[0]=='#') continue;		//		commentary

				char* brk = strpbrk(line,"=");
				if (brk==NULL) continue;		//		no "=" on this line
				*brk++ = 0;		//		separates key and value

				char* key = tolower(trim(line));
				char* value = trim(brk);

				if (*key==0) continue;
				if (*value==0) continue;
		//		printf("%s = %s \n",key,value);

				if (strcmp(key,"jvm")==0)
					local_jvm_path.add(newString(value));
				else if (strcmp(key,"version")==0)
					preferred_version.parse(value);
				else if (strcmp(key,"cp")==0)
					jvm->setClassPath(newString(value));
				else if (strcmp(key,"lp")==0)
					jvm->setLibraryPath(newString(value));
				else if (strcmp(key,"arg")==0)
					main_args.parse(value);
				else if (strcmp(key,"jvm_arg")==0)
					jvm_options.parse(value);
				else if (strcmp(key,"splash")==0)
					splash.parse(value);
				else if (strcmp(key,"main")==0)
					main_class = replace(newString(value),'.','/');
			}
	}

	/*
	 *	parse command line args
	 */
//	jvm.setClassPath(class_path);
//	jvm.setLibraryPath(library_path);

	int argc = argv->size();
	for (int i=1; i<argc; i++)
	{
		if (strcmp("-jvm",argv->get(i))==0 && (i+1) < argc)
			local_jvm_path.add(argv->get(++i));
		else if (strcmp("-version",argv->get(i))==0 && (i+1) < argc)
			preferred_version.parse(argv->get(++i));
		else if (strcmp("-main",argv->get(i))==0 && (i+1) < argc)
			main_class = replace((char*)argv->get(++i),'.','/');
		else if (strcmp("-cp",argv->get(i))==0 && (i+1) < argc)
			jvm->setClassPath(argv->get(++i));
		else if (strcmp("-lp",argv->get(i))==0 && (i+1) < argc)
			jvm->setLibraryPath(argv->get(++i));
		else if (strncmp("-D",argv->get(i),2)==0 || strncmp("-X",argv->get(i),2)==0)
			jvm_options.add(argv->get(i));			//	this is a JVM argument
		else if (strncmp("-J",argv->get(i),2)==0)
			jvm_options.add(argv->get(i)+2);		//	this is a JVM argument starting with -J
		else
			main_args.add(argv->get(i));	//	this is a MAIN argument
	}

	/*
	 * set work dir to application location
	 */
	const char* work_dir = getWorkDir(argv->get(0));
	if (work_dir != NULL && work_dir[0] != 0)
		setCurrentDirectory(work_dir);
		//		otherwise: keep current dir

	/*
	 *	show splash screen
	 */
	if (showSplashScreen(splash))
		main_args.add("splash=off");	//	tell the application not to show its own splash screen


	/**
     * TODO find correct JDK, either in working dir, or from Registry
	 */
	const char* jvm_path = JVM::find(&local_jvm_path, &preferred_version);

	int error = jvm->launch(jvm_path, JNI_VERSION_21, &jvm_options);
	if (error >= 0)
		error = jvm->call(main_class,&main_args);

	if (error < 0)
		switch (error)
		{
		case JVM::JVM_ERROR_DLL_MISSING:				fatal(MISSING_JRE,error);
		case JVM::JVM_ERROR_DLL_NOT_FOUND:				fatal("jvm.dll not found",error);
		case JVM::JVM_ERROR_CREATE_JAVA_VM_NOT_FOUND:	fatal("JNI method not found",error);
		case JVM::JVM_ERROR_CREATE_JAVA_VM_FAILED:		fatal("CreateJavaVM failed",error);
		case JVM::JVM_ERROR_MAIN_CLASS_NOT_FOUND:		fatal("Main class not found",error);
		case JVM::JVM_ERROR_MAIN_METHOD_NOT_FOUND:		fatal("main method not found",error);
		case JVM::JVM_ERROR_BAD_MAIN_ARGS:				fatal("bad arguments to main()",error);
		default:										fatal("failed to launch JVM",-1);
		}

	hideSplashScreen();

    /* destroy immediately. AWT threads will keep running. */
    jvm->destroy();

	return +1;
}


jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	return JNI_VERSION_21;
}

