package com.dwayne.com.stream.sender.sendqueue;


import com.dwayne.com.entity.Frame;

public interface ISendQueue {
    void start();
    void stop();
    void setBufferSize(int size);
    void putFrame(Frame frame);
    Frame takeFrame();
    void setSendQueueListener(SendQueueListener listener);
}
