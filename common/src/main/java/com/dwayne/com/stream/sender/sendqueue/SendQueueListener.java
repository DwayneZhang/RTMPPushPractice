package com.dwayne.com.stream.sender.sendqueue;


public interface SendQueueListener {
    void good();
    void bad();
}
