

//#include <stdio.h>
#include <jni.h>
#include <windows.h>

#include "jvm.h"
#include "util.h"

#include "jlaunch.h"

void message(const char* msg, const char* title, int code)
{
	char* temp = new char[64];
	if (code!=0) {
		sprintf(temp," error code = %i",code);
		MessageBox(NULL, stringcat(msg,temp,NULL),title,MB_OK);
	}
	else
		MessageBox(NULL, msg,title,MB_OK);
}

void fatal(const char* msg, int code)
{
//	fprintf(stderr,"%s\n",message);
	//	show message box
	message(msg,"Error",code);
	exit(code);
}


void setCurrentDirectory(const char* dir) {
	BOOL ok = SetCurrentDirectory(dir);
	if (!ok) message("could not set current directory","Warning",0);
}


extern StringList splash;

//	Splash Screen Window Handle
HWND splash_hwnd = NULL;

/**	splash screen	
	get(0) = path to jpeg
	get(1..) additional text
*/

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
HWND showWinSplashScreen(const char* path)
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


bool showSplashScreen(const StringList& splash)
{
	if (hInstance!=NULL && splash.size()>0)
	{
		splash_hwnd = showWinSplashScreen(splash.get(0));
		if (splash_hwnd!=NULL)
			return true;
	}
    return false;
}

void hideSplashScreen()
{
	if (splash_hwnd!=NULL)
		DestroyWindow(splash_hwnd);
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
