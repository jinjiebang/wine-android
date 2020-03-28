

cmake_minimum_required(VERSION 3.4.1)


add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/native-lib.cpp
             src/main/jni/AIWine.cpp
             src/main/jni/AIWine.h
             src/main/jni/Chess.cpp
             src/main/jni/Chess.h
             src/main/jni/Board.cpp
             src/main/jni/Board.h
             src/main/jni/HashTable.h
             src/main/jni/AITypes.h
             src/main/jni/ChessShape.cpp
             src/main/jni/ChessShape.h)
include_directories(src/main/jni/)


find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )



target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )