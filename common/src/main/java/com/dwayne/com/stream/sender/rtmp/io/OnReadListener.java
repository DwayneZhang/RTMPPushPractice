package com.dwayne.com.stream.sender.rtmp.io;


import com.dwayne.com.stream.sender.rtmp.packets.Chunk;

public interface OnReadListener {
    void onChunkRead(Chunk chunk);
    void onDisconnect();
    void onStreamEnd();
}
