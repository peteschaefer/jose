cmake_minimum_required(VERSION 3.30)
project(winUtils)

set(CMAKE_CXX_STANDARD 17)

include_directories(
        "C:\\Program Files\\Java\\jdk-21.0.6\\include",
        "C:\\Program Files\\Java\\jdk-21.0.6\\include\\win32",
)

add_library(winUtils SHARED winRegistry.cpp winUtils.cpp)
