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

void setCurrentDirectory(const char*);
bool showSplashScreen(const StringList& splash);
void hideSplashScreen();

int launch(StringList* argv);

jint JNI_OnLoad(JavaVM *vm, void *reserved);


#endif //JLANUCH_H
