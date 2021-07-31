package com.dwayne.com.controller.audio;


import com.dwayne.com.audio.OnAudioEncodeListener;
import com.dwayne.com.configuration.AudioConfiguration;

public interface IAudioController {
    void start();
    void stop();
    void pause();
    void resume();
    void mute(boolean mute);
    int getSessionId();
    void setAudioConfiguration(AudioConfiguration audioConfiguration);
    void setAudioEncodeListener(OnAudioEncodeListener listener);
}
