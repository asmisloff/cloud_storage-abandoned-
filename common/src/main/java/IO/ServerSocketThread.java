package IO;

import java.io.*;
import java.net.Socket;

public class ServerSocketThread extends SocketThread {

    public ServerSocketThread(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public synchronized void run() {
        listenClient();
    }

    private void login() throws IOException {
        sendResponce(0);
        whaitForInputData(1000);
        username = is.readUTF();
        sendResponce(0);
        FileUtility.createDirectory("./common/server/" + username + "/");
    }

    private void listenClient() {
        String request = new String();
        while (!isInterrupted()) {
            try {
                if (is.available() > 0) {
                    request = is.readUTF();
                    System.out.println(request);
                    switch (request) {
                        case "*LOGIN*":
                            login();
                            break;
                        case "*INFO*":
                            sendListOfFiles();
                            break;
                        case "*UPLOAD*":
                            uploadFile();
                            break;
                        case "*DOWNLOAD*":
                            sendFile();
                        case "*DISCONNECT*":
                            interrupt();
                            return;
                    }
                    sleep(1000);
                }
                } catch(IOException | InterruptedException e){
                    e.printStackTrace();
                }
        }
    }

    private void sendListOfFiles() throws IOException {
        sendResponce(0);
        StringBuilder sb = new StringBuilder();
        FileUtility.getAllFiles(new File("./common/server/" + username), sb);
        os.writeUTF(sb.toString());
    }

    private void sendFile() throws IOException {
        sendResponce(0);
        whaitForInputData(1000);
        String path = String.format("./common/server/%s/%s", username, is.readUTF());
        System.out.println(path);

        File file = new File(path);
        if (file.exists()) {
            sendResponce(file.length());
        } else {
            sendResponce(-1);
            return;
        }

        try (InputStream fs = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            while (fs.available() > 0) {
                int readBytes = fs.read(buffer);
                os.write(buffer, 0, readBytes);
            }
        }
    }

    public void uploadFile() throws IOException {
        sendResponce(0);
        whaitForInputData(1000);
        String fileName = is.readUTF();
        System.out.println("fileName: " + fileName);
        File file = new File(String.format("./common/server/%s/%s", username, fileName));
        file.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[8192];
            long bytesRead = 0;
            while (is.available() > 0) {
                int r = is.read(buffer);
                bytesRead += r;
                fos.write(buffer, 0, r);
            }
            System.out.printf("File uploaded! Size: %d\n", bytesRead);
            sendResponce(bytesRead);
        }
    }

    public void sendResponce(long resp) throws IOException {
        os.writeLong(resp);
    }
}
