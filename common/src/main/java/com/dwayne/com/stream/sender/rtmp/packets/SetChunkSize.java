package com.dwayne.com.stream.sender.rtmp.packets;


import com.dwayne.com.stream.sender.rtmp.Util;
import com.dwayne.com.stream.sender.rtmp.io.SessionInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SetChunkSize extends Chunk {

    private int chunkSize;

    public SetChunkSize(ChunkHeader header) {
        super(header);
    }

    public SetChunkSize(int chunkSize) {
        super(new ChunkHeader(ChunkType.TYPE_1_LARGE, SessionInfo.RTMP_CONTROL_CHANNEL, MessageType.SET_CHUNK_SIZE));
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public void readBody(InputStream in) throws IOException {
        // Value is received in the 4 bytes of the body
        chunkSize = Util.readUnsignedInt32(in);
    }

    @Override
    protected void writeBody(OutputStream out) throws IOException {
        Util.writeUnsignedInt32(out, chunkSize);
    }
}
