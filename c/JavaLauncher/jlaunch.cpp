

//#include <stdio.h>
#include <jni.h>
#include <windows.h>

#include "jvm.h"
#include "util.h"

#define MISSING_JRE 	"Java Runtime Environment 1.4 or later is required\n" \
						"Please read the download instructions at \n " \
						"      http://jose-chess.sourceforge.net/download.html\n" \
						"or download Java from \n" \
						"      http://java.sun.com"

static void message(char* msg, char* title, int code)
{
	char* temp = new char[64];
	if (code!=0) {
		sprintf(temp," error code = %i",code);
		MessageBox(NULL, stringcat(msg,temp,NULL),title,MB_OK);
	}
	else
		MessageBox(NULL, msg,title,MB_OK);
}

static void fatal(char* msg, int code)
{
//	fprintf(stderr,"%s\n",message);
	//	show message box
	message(msg,"Error",code);
	exit(code);
}



char* getIniFile(char* arg0)
{
	//		trim trailing ".exe"
	int len = strlen(arg0);
	if (len > 4 && strcmp(arg0+len-4,".exe")==0)
		arg0[len-4] = 0;
	else if (len > 4 && strcmp(arg0+len-4,".EXE")==0)
		arg0[len-4] = 0;
	return stringcat(arg0,".ini",NULL);
}

char* getWorkDir(char* arg0)
{
	char* s = arg0+strlen(arg0);
	while (s > arg0 && s[-1] != '\\')
		s--;
	s[0] = 0;
	return arg0;
}


//	Splash Screen Window Handle
HWND splash_hwnd = NULL;

/**	splash screen	
	get(0) = path to jpeg
	get(1..) additional text
*/
StringList splash;
//	application instance
HINSTANCE hInstance;
//	splash screen bitmap handle
HBITMAP bitmap;
//	and actual bitmap
BITMAP bm;


//	Window Procedure for Splash Screen
LRESULT CALLBACK SplashWndProc(HWND hWnd, UINT iMessage, WPARAM wParam, LPARAM lParam)
{
	switch(iMessage)
	{
		case WM_PAINT:
			PAINTSTRUCT   ps;
			HBITMAP       hOldBitmap;
			HDC           hDC, hMemDC;

			hDC = BeginPaint( hWnd, &ps );
			
			hMemDC = CreateCompatibleDC( hDC );
			hOldBitmap = (HBITMAP)SelectObject( hMemDC, bitmap );

			BitBlt( hDC, 0, 0, bm.bmWidth, bm.bmHeight,
					hMemDC, 0, 0, SRCCOPY );

			SelectObject( hMemDC, hOldBitmap );
			DeleteObject( bitmap );

			HFONT hfnt = (HFONT)GetStockObject(ANSI_VAR_FONT); 
			SelectObject(hDC, hfnt);
	    
			for (int i=1; i < splash.size(); i++)
			{			
				TextOut(hDC, 10, bm.bmHeight-10+(i-splash.size())*20, splash.get(i), splash.length(i)); 
			}

			EndPaint( hWnd, &ps );

		break;
	}

	return DefWindowProc(hWnd, iMessage, wParam, lParam);
}

//	register window class
BOOL InitWindowClass() 
{ 
    WNDCLASSEX wcx; 
 
    wcx.cbSize = sizeof(wcx);          // size of structure 
    wcx.style = CS_HREDRAW | CS_VREDRAW;                    // redraw if size changes 
    wcx.lpfnWndProc = SplashWndProc;     // points to window procedure 
    wcx.cbClsExtra = NULL;                // no extra class memory 
    wcx.cbWndExtra = NULL;                // no extra window memory 
    wcx.hInstance = hInstance;         // handle to instance 
    wcx.hIcon = LoadIcon(hInstance, NULL);              // predefined app. icon 
    wcx.hCursor = LoadCursor(NULL, IDC_ARROW);                    // predefined arrow 
    wcx.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);                  // white background brush 
    wcx.lpszMenuName =  NULL;    // name of menu resource 
    wcx.lpszClassName = "SplashWClass";  // name of window class 
    wcx.hIconSm = NULL; 
 
    return RegisterClassEx(&wcx); 
} 


//	create and open splash screen window
HWND ShowSplashScreen(char* path)
{
	if (!InitWindowClass()) return NULL;

	bitmap = (HBITMAP)LoadImage( NULL, splash.get(0), IMAGE_BITMAP, 0, 0,
               LR_CREATEDIBSECTION | LR_DEFAULTSIZE | LR_LOADFROMFILE );

	GetObject(bitmap, sizeof(BITMAP), &bm);

	//	calc screen size
	HDC device = GetDC(NULL);
	int screen_width = GetDeviceCaps(device,HORZRES);
	int screen_height = GetDeviceCaps(device,VERTRES);

	//	create window
	HWND hwnd = CreateWindowEx(WS_EX_TOPMOST|WS_EX_TOOLWINDOW, //	always on top, not in taskbar
		"SplashWClass", NULL, WS_POPUP,			//	no border
		(screen_width-bm.bmWidth)/2,
		(screen_height-bm.bmHeight)/2,
		bm.bmWidth, bm.bmHeight,
		(HWND)NULL, (HMENU)NULL, hInstance, NULL);

	if (hwnd==NULL) return NULL;

	//	show it
	ShowWindow(hwnd, SW_SHOWNORMAL); 
	UpdateWindow(hwnd); 

	return hwnd;
}

void HideSplashScreen(HWND hwnd)
{
	DestroyWindow(hwnd);
}

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
	char* ini_file = getIniFile(argv->get(0));

	if (ini_file != NULL) {
			FILE* file = fopen(ini_file,"r");
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
			main_class = replace(argv->get(++i),'.','/');
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
	char* work_dir = getWorkDir(argv->get(0));
	if (work_dir != NULL && work_dir[0] != 0)
		SetCurrentDirectory(work_dir);
		//		otherwise: keep current dir

	/*
	 *	show splash screen
	 */	
	if (hInstance!=NULL && splash.size()>0)
	{
		if ((splash_hwnd=ShowSplashScreen(splash.get(0)))!=NULL)
			main_args.add("splash=off");	//	tell the application not to show its own splash screen
	}

	/**
     * TODO find correct JDK, either in working dir, or from Registry
	 */
	char* jvm_path = JVM::find(&local_jvm_path, &preferred_version);

	int error = jvm->launch(jvm_path, JNI_VERSION_1_4, &jvm_options);	
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
		
	if (splash_hwnd!=NULL)
		HideSplashScreen(splash_hwnd);

    /* destroy immediately. AWT threads will keep running. */ 
    jvm->destroy();

	return +1;
}


/**
 * main entry point for console application
 */
int main(int argc, char** argv)
{
	return launch(new StringList(argc,argv));
}

/**
 * Main Entry point for Windows application
 */
int APIENTRY WinMain(HINSTANCE hinst,
                     HINSTANCE hPrevInstance,
                     LPSTR     lpCmdLine,
                     int       nCmdShow )
{
	hInstance = hinst;
	char* cmdline = GetCommandLineA();

	StringList* args = new StringList();
	args->parse1(cmdline);
	args->parse(lpCmdLine);

	return launch(args);
}
