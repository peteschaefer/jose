cmake_minimum_required(VERSION 3.0)
project(JavaLauncher)

option(WINDOWS "build for Windows" OFF)
option(UNIX "build for Unixes" OFF)
option(DARWIN "build for Unixes" ON)

set(sources
        jlaunch.cpp
        jvm.cpp
        strlist.cpp
        util.cpp)

if (WINDOWS)
    include_directories(
            "C:/Program Files (x86)/Java/jdk1.8.0_202/include"
            "C:/Program Files (x86)/Java/jdk1.8.0_202/include/win32"
    )
    add_compile_definitions(WIN_REGISTRY)

    list(APPEND sources
            jlaunch-win.cpp
            winreg.cpp)
endif ()

if (DARWIN)
    include_directories(
            "/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/include"
            "/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/include/darwin"
    )

    list(APPEND sources
            jlaunch-unix.cpp)
endif ()

add_executable(JavaLauncher ${sources})