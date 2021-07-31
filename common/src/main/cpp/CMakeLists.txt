cmake_minimum_required(VERSION 3.10.2)

#定义 ffmpeg、rtmp 、player 目录
#ffmpeg
set(FFMPEG ${CMAKE_SOURCE_DIR}/ffmpeg)
#拉流-推流
set(RTMP ${CMAKE_SOURCE_DIR}/librtmp)
#播放器
set(player ${CMAKE_SOURCE_DIR}/player)
#推流器
set(pusher ${CMAKE_SOURCE_DIR}/pusher)
#视频编码器
set(x264 ${CMAKE_SOURCE_DIR}/x264)
#语音编码器
set(faac ${CMAKE_SOURCE_DIR}/faac)

#utils 工具类
set(utils ${CMAKE_SOURCE_DIR}/utils)

#加载 ffmpeg 头文件目录
include_directories(${FFMPEG}/include)

#加载 rtmp 头文件目录
include_directories(${RTMP}/include)

#加载 x264 头文件目录
include_directories(${x264}/include)

#加载 faac 头文件目录
include_directories(${faac}/include)

#加载 UTIS 头文件
include_directories(${utils}/)

#指定 ffmpeg 静态库文件目录
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${FFMPEG}/libs/${CMAKE_ANDROID_ARCH_ABI}")

#指定 rtmp 静态库文件目录
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${RTMP}/libs/${CMAKE_ANDROID_ARCH_ABI}")

#指定 x264 静态库文件目录
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${x264}/libs/${CMAKE_ANDROID_ARCH_ABI}")

#指定 faac 静态库文件目录
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${faac}/libs/${CMAKE_ANDROID_ARCH_ABI}")

#批量添加自己编写的 cpp 文件,不要把 *.h 加入进来了
file(GLOB Player_CPP ${player}/*.cpp)
file(GLOB Push_CPP ${pusher}/*.cpp)

#添加自己编写 cpp 源文件生成动态库
add_library(player SHARED ${Player_CPP})

#添加自己编写 cpp 源文件生成动态库
add_library(pusher SHARED ${Push_CPP})

#找系统中 NDK log库
find_library(log_lib
        log)


#播放 so
target_link_libraries(
        #播放 so
        player
        # 写了此命令不用在乎添加 ffmpeg lib 顺序问题导致应用崩溃
        -Wl,--start-group
        avcodec avfilter avformat avutil swresample swscale
        -Wl,--end-group
        z
        #拉流库
        rtmp
        #本地库
        android
        #音频播放
        OpenSLES
        #        utils
        ${log_lib})

#推流 so
target_link_libraries(
        #播放 so
        pusher
        #        # 写了此命令不用在乎添加 ffmpeg lib 顺序问题导致应用崩溃
        #        -Wl,--start-group
        #        avcodec avfilter avformat avutil swresample swscale
        #        -Wl,--end-group
        #        z
        #推流库
        rtmp
        #视频编码
        x264
        #语音编码
        faac
        #本地库
        android
        ${log_lib}
)
