package com.dwayne.com.stream.sender.rtmp.io;


import com.dwayne.com.stream.sender.rtmp.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StoreChunk {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 128);

    /** @return <code>true</code> if all packet data has been stored, or <code>false</code> if not */
    public boolean storeChunk(InputStream in, int messageLength, int chunkSize) throws IOException {
        final int remainingBytes = messageLength - baos.size();
        byte[] chunk = new byte[Math.min(remainingBytes, chunkSize)];
        Util.readBytesUntilFull(in, chunk);
        baos.write(chunk);
        return (baos.size() == messageLength);
    }

    public ByteArrayInputStream getStoredInputStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        baos.reset();
        return bis;
    }

    /** Clears all currently-stored packet chunks (used when an ABORT packet is received) */
    public void clearStoredChunks() {
        baos.reset();
    }
}
