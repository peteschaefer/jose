cmake_minimum_required(VERSION 3.0)
project(JavaLauncher)

option(WINDOWS "build for Windows" ON)
option(UNIX "build for Unixes" OFF)
option(DARWIN "build for Unixes" OFF)

set(sources
        jlaunch.cpp
        jvm.cpp
        strlist.cpp
        util.cpp)

set(JDK "C:\\Users\\nightrider\\Downloads\\jbrsdk-21.0.6-windows-x64-b825.77\\jbrsdk-21.0.6-windows-x64-b825.77")
include_directories(
        "${JDK}\\include"
        "${JDK}\\include\\win32"
)
add_compile_definitions(WIN_REGISTRY)

list(APPEND sources
        jlaunch-win.cpp
        winreg.cpp)

add_executable(JavaLauncher ${sources})