//
// Created by nightrider on 25.01.2025.
//

#ifndef JLANUCH_H
#define JLANUCH_H


#include "strlist.h"

const char* getIniFile(const char* arg0);
const char* getWorkDir(const char* arg0);

void message(const char* msg, const char* title, int code);
void fatal(const char* msg, int code);

void SetCurrentDirectory(const char*);
bool ShowSplashScreen(const StringList& splash);
void HideSplashScreen();

int launch(StringList* argv);

#endif //JLANUCH_H
