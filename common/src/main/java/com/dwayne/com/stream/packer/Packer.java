package com.dwayne.com.stream.packer;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface Packer {
    interface OnPacketListener {
        //第一个参数为打包后的数据，第二个为自定义打包后的类型
        void onPacket(byte[] data, int packetType);



    }
    //设置打包监听器
    void setPacketListener(OnPacketListener listener);
    //处理视频硬编编码器输出的数据
    void onVideoData(ByteBuffer bb, MediaCodec.BufferInfo bi);
    //处理音频硬编编码器输出的数据
    void onAudioData(ByteBuffer bb, MediaCodec.BufferInfo bi);
    //开始打包，一般进行打包的预处理
    void start();
    //结束打包，一般进行打包器的状态恢复
    void stop();


    interface OnAudioPacketListener extends OnPacketListener  {
        int getInputSamples();

        void sendAudioInfo(int frequency, int channels);
    }

    interface OnVideoPacketListener extends OnPacketListener  {
    }
}
