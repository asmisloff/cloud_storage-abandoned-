package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    //todo: List of ServerSocketThreads, stop()

    ServerSocket server;

    public Server() throws IOException {
        server = new ServerSocket(8189);
        System.out.println("Server started");
    }

    public void listenForClientConnection() {
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Client accepted!");
                ServerSocketThread sst = new ServerSocketThread(socket);
                sst.start();
                System.out.println("New socket thread started");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listenForClientConnection();
    }
}
