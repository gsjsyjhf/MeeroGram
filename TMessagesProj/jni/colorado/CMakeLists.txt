cmake_minimum_required(VERSION 3.22.1)
# MeeroGram stub: upstream colorado native module is not published.
# jni/ must stay on the include path: colorado.cpp and jni.c both use
#include "colorado/colorado.h" (path relative to jni/).
include_directories(${CMAKE_HOME_DIRECTORY})
add_library(colorado STATIC ${CMAKE_CURRENT_LIST_DIR}/colorado.cpp)
