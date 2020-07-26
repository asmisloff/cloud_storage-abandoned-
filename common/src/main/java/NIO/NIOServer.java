package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;

public class NIOServer implements Runnable {

    public static final String ROOT = "./common/src/resources/serverFiles";

    private ServerSocketChannel server;
    private Selector selector;
    HashMap<SocketChannel, ChannelHandler> channelHandlers;

    public NIOServer() throws IOException {
        server = ServerSocketChannel.open();
        server.socket().bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        channelHandlers = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            System.out.println("server started");
            while (server.isOpen()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        System.out.println("client accepted");
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        channelHandlers.put(channel, new ChannelHandler(channel));
                    }
                    if (key.isReadable()) {
                        System.out.println("Read key: " + key.toString());
                        ChannelHandler ch = channelHandlers.get(key.channel());
                        ch.readRequest();
                        if (ch.tryParseRequest()) {
                            System.out.printf("Request parsed: %s -- %s\n", ch.getRequestTag(), ch.getRequestParameter());
                            ch.executeRequest();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Thread(new NIOServer()).start();
    }
}
