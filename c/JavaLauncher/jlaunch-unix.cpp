

//#include <stdio.h>
#include <cstdlib>
#include <cstring>
#include <jni.h>

#include "jlaunch.h"
#include "jvm.h"
#include "util.h"

void message(const char* msg, const char* title, int code)
{
	printf("--- %s ---\n",title);
	if (code!=0)
		printf("error code = %i\n",code);
	printf("%s\n", msg);
}

void fatal(const char* msg, int code)
{
//	fprintf(stderr,"%s\n",message);
	//	show message box
	message(msg,"Error",code);
	exit(code);
}

void SetCurrentDirectory(const char*) {

}

bool ShowSplashScreen(const StringList& splash) {
	//	no-op
	return false;
}

void HideSplashScreen() {
	//	no-op
}

/**
 * main entry point for console application
 */
int main(int argc, char** argv)
{
	return launch(new StringList(argc,argv));
}
