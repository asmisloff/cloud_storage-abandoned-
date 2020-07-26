package NIO;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChannelHandler {
    private byte[] buffer;
    private int size; // current size of buffer
    private String requestTag;
    private String requestParameter;
    private ByteBuffer bb;
    private SocketChannel channel;
    private int userID;

    public ChannelHandler(SocketChannel channel) {
        buffer = new byte[1028];
        size = 0;
        requestParameter = null;
        requestTag = null;
        bb = ByteBuffer.allocate(512);
        this.channel = channel;
    }

    private void setup() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
        size = 0;
    }

    public boolean tryParseRequest() throws UnsupportedEncodingException {
        if (buffer[size - 1] != '\0') {
            return false;
        }
        String rs = new String(buffer, 0, size - 2, "UTF16");
        String[] parts = rs.split(" ");
        requestTag = parts[0];
        requestParameter = parts[1];
        userID = Integer.parseInt(parts[2]);
        return true;
    }

    public void readRequest() throws IOException {
        int count = channel.read(bb);
        if (count == -1) {
            channel.close();
            System.out.println("Channel closed");
            return;
        }
        bb.flip();
        while (bb.hasRemaining()) {
            buffer[size++] = bb.get();
        }
        bb.clear();
    }

    public void sendString(String s) throws IOException {
        bb.clear();
        bb.put(s.getBytes("UTF16"));
        bb.flip();
        channel.write(bb);
    }

    private void sendLong(long n) throws IOException {
        bb.clear();
        bb.putLong(n);
        bb.flip();
        channel.write(bb);
    }

    private void sendByte(byte b) throws IOException {
        bb.clear();
        bb.put(b);
        bb.flip();
        channel.write(bb);
    }

    public void executeRequest() throws IOException {
        switch (requestTag) {
            case "./download":
                Path p = Paths.get(NIOServer.ROOT, "user" + userID, requestParameter);
                System.out.println(p.toString());
                if (Files.exists(p)) {
                    sendByte((byte) 0);
                    sendLong(Files.size(p));
                    RandomAccessFile raf = new RandomAccessFile(p.toString(), "rw");
                    FileChannel fch = raf.getChannel();
                    bb.clear();
                    while (fch.read(bb) != -1 ) {
                        bb.flip();
                        channel.write(bb);
                        bb.clear();
                    }
                }
                break;
        }
        setup();
    }

    public String getRequestTag() {
        return requestTag;
    }

    public String getRequestParameter() {
        return requestParameter;
    }
}
