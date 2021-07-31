#include "include/Pusher.h"


/**
 * 对音视频推流变量统一管理
 */

Pusher *mPusher;
JavaVM *javVM = 0;


/**
 * JNI 初始化加载，最先加载
 * @param javaVM
 * @param pVoid
 * @return
 */
int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    javVM = javaVM;
    return JNI_VERSION_1_6;;
}


/**
 * native 初始化
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1init(JNIEnv *env, jobject instance,
                                                         jboolean isMediaCodec) {
    LOGD("init");
    mPusher = new Pusher();
    mPusher->init(javVM, env, isMediaCodec, instance);

}


/**
 * 开始推流
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1push_1video(JNIEnv *env, jobject instance,
                                                                jbyteArray data_) {
    //查看是否可以开始推流工作
    if (mPusher->mVideoChannel && mPusher->isReadyPushing()) {
        jbyte *data = env->GetByteArrayElements(data_, NULL);
        LOGD("push");
        mPusher->mVideoChannel->encodeData(data);
        env->ReleaseByteArrayElements(data_, data, 0);
    }

}

/**
 * 设置编码信息
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1setVideoEncoderInfo(JNIEnv *env,
                                                                        jobject instance, jint w,
                                                                        jint h, jint mFps,
                                                                        jint mBit) {

    LOGD("width: %d , height: %d fps: %d bit: %d", w, h, mFps, mBit);
    if (mPusher && mPusher->mVideoChannel) {
        mPusher->mVideoChannel->setVideoEncoderInfo(w, h, mFps, mBit);
    }


}


/**
 * 开始直播
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1start(JNIEnv *env, jobject instance,
                                                          jstring path_) {
    LOGD("start");
    if (mPusher && mPusher->isReadyPushing()) {
        return;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    if (mPusher) {
        mPusher->start(path);

    }
    env->ReleaseStringUTFChars(path_, path);
}


/**
 * 释放
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1release(JNIEnv *env, jobject instance) {
    LOGD("release");
    if (mPusher) {
        mPusher->release();
        delete mPusher;
        mPusher = 0;
    }
}

/**
 * 设置音频编码配置
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1setAudioEncInfo(JNIEnv *env, jobject instance,
                                                                    jint sampleRate,
                                                                    jint channels) {

    if (mPusher && mPusher->mAudioChannel) {
        mPusher->mAudioChannel->setAudioEncoderInfo(sampleRate, channels);
    }
}

/**
 * 获取一次音频编码输入的样本数量
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_dwayne_com_pusher_PusherManager_getInputSamples(JNIEnv *env, jobject instance) {

    if (mPusher && mPusher->mAudioChannel) {
        return mPusher->mAudioChannel->getInputSamples();
    }
    return -1;

}

/**
 * 接收 Java 端的音频 PCM 数据
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1pushAudio(JNIEnv *env, jobject instance,
                                                              jbyteArray audioData_) {

    if (mPusher && mPusher->mAudioChannel && mPusher->isReadyPushing()) {
        jbyte *audioData = env->GetByteArrayElements(audioData_, NULL);
        mPusher->mAudioChannel->encodeData(audioData);
        env->ReleaseByteArrayElements(audioData_, audioData, 0);
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_pushAACData(JNIEnv *env, jobject instance,
                                                        jbyteArray audio_, jint length,
                                                        int type) {
    jbyte *audio = env->GetByteArrayElements(audio_, NULL);

    if (mPusher && mPusher->mAudioChannel && mPusher->isReadyPushing())
        mPusher->mAudioChannel->pushAAC(reinterpret_cast<u_char *>(audio), length, type);

    env->ReleaseByteArrayElements(audio_, audio, 0);
}




extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_pushH264(JNIEnv *env, jobject instance,
                                                     jbyteArray video_, int type,
                                                     long timestamp) {
    jbyte *video = env->GetByteArrayElements(video_, NULL);

    if (mPusher && mPusher->mVideoChannel && mPusher->isReadyPushing())
        mPusher->mVideoChannel->sendH264(type, reinterpret_cast<uint8_t *>(video),
                                           env->GetArrayLength(video_), timestamp);

    env->ReleaseByteArrayElements(video_, video, 0);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_dwayne_com_pusher_PusherManager_native_1mediacodec(JNIEnv *env, jobject instance,
                                                               jboolean b) {


    if (mPusher){
        mPusher->setMediaCodec(b);
    }
}






int Pusher::isReadyPushing() {
    return mRtmpManager->readyPushing;
}
