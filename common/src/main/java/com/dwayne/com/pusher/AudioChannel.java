package com.dwayne.com.pusher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.dwayne.com.audio.AudioProcessor;
import com.dwayne.com.capture.AudioCapture;
import com.dwayne.com.configuration.AudioConfiguration;
import com.dwayne.com.constant.SopCastConstant;
import com.dwayne.com.stream.packer.Packer;
import com.dwayne.com.stream.packer.rtmp.RtmpPacker;
import com.dwayne.com.utils.SopCastLog;

public class AudioChannel extends BaseChannel {
    private AudioCapture mAudioCapture;


    //准备录音机 采集pcm 数据
    int channelConfig;


    int channels = 1;

    /**
     * 是否开始
     */
    boolean isLive = false;
    private AudioConfiguration mAudioConfiguration;
    private Packer.OnAudioPacketListener mOnPacketListener;
    private AudioRecord mAudioRecord;
    private AudioProcessor mAudioProcessor;

    public AudioChannel(AudioConfiguration audioConfiguration, Packer.OnAudioPacketListener onPacketListener) {
        mAudioConfiguration = audioConfiguration;
        mOnPacketListener = onPacketListener;
        initAudioCapture();
    }

    private void initAudioCapture() {

        //最小需要的缓冲区

        mAudioCapture = new AudioCapture();

        if (channels == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        } else {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        }

        mOnPacketListener.sendAudioInfo(mAudioConfiguration.frequency,channels);

        //16 位 2个字节
       int inputSamples = mOnPacketListener.getInputSamples() * 2;


        mAudioCapture.setOnAudioFrameCapturedListener(new AudioCapture.OnAudioFrameCapturedListener() {
            @Override
            public void onAudioFrameCaptured(byte[] audioData) {
                if (isLive){
                    mOnPacketListener.onPacket(audioData, RtmpPacker.PCM);
                }
            }
        });

        //最小需要的缓冲区
        int minBufferSize = AudioRecord.getMinBufferSize(mAudioConfiguration.frequency, channelConfig, AudioFormat.ENCODING_PCM_16BIT) * 2;
        mAudioCapture.startCapture(MediaRecorder.AudioSource.MIC,mAudioConfiguration.frequency,channelConfig,AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize > inputSamples ? minBufferSize : inputSamples,inputSamples);
    }


    public void start() {
        SopCastLog.d(SopCastConstant.TAG, "Audio Recording start");
    }

    @Override
    public void startLive() {
        isLive = true;

    }

    @Override
    public void stopLive() {
        isLive = false;
    }

    @Override
    public void release() {
        isLive = false;
        mAudioCapture.stopCapture();
    }

    @Override
    public void onRestart() {
        isLive = true;
    }
}
