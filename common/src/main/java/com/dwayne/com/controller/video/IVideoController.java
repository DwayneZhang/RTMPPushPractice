package com.dwayne.com.controller.video;


import com.dwayne.com.configuration.VideoConfiguration;
import com.dwayne.com.video.OnVideoEncodeListener;

public interface IVideoController {
    void start();
    void stop();
    void pause();
    void resume();
    boolean setVideoBps(int bps);
    void setVideoEncoderListener(OnVideoEncodeListener listener);
    void setVideoConfiguration(VideoConfiguration configuration);
}
