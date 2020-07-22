package IO;

import java.io.*;
import java.net.Socket;

public class ClientSocketThread extends SocketThread {

    public ClientSocketThread(String host, int port, String username) throws IOException {
        super(new Socket(host, port));
        this.username = username;
    }

    public boolean login() throws IOException {
        FileUtility.createDirectory("./common/client/" + username);
        os.writeUTF("*LOGIN*");
        checkResponse(0);
        os.writeUTF(username);
        return checkResponse(0);
    }

    public boolean uploadFile(File file) throws IOException {
        InputStream fs = new FileInputStream(file);
        long size = file.length();
        int count = (int) (size / 8192) / 10, readBuckets = 0;
        // /==========/
        byte [] buffer = new byte[8192];
        os.writeUTF("*UPLOAD*");
        os.flush();

        if (!checkResponse(0)) {
            return false;
        }

        os.writeUTF(file.getName());
        System.out.print("/");
        while (fs.available() > 0) {
            int readBytes = fs.read(buffer);
            readBuckets++;
            if (readBuckets % count == 0) {
                System.out.print("=");
            }
            os.write(buffer, 0, readBytes);
        }
        os.flush();
        System.out.println("/");

        return checkResponse(file.length());
    }

    public boolean downloadFile(String path) throws IOException {
        os.writeUTF("*DOWNLOAD*");
        if (!checkResponse(0)) {
            return false;
        }

        os.writeUTF(path);
        long expectedFileSize = getResponse();
        if (expectedFileSize < 0) { // no file
            return false;
        }

        try (FileOutputStream fos = new FileOutputStream(String.format("common/client/%s/%s", username, path))) {
            byte[] buffer = new byte[8192];
            long bytesRead = 0;
            while (is.available() > 0) {
                int r = is.read(buffer);
                bytesRead += r;
                fos.write(buffer, 0, r);
            }
            System.out.printf("File downloaded! Size: %d. Expected: %d\n", bytesRead, expectedFileSize);
            return bytesRead == expectedFileSize;
        }
    }

    public String getFileInfo() throws IOException {
        sendRequest("*INFO*");
        if (!checkResponse(0)) {
            return null;
        }
        return is.readUTF();
    }

    @Override
    public void run() {
        super.run();
    }

    public long getResponse() throws IOException {
        whaitForInputData(1000);
        return is.readLong();
    }

    public boolean checkResponse(long expected) {
        try {
            long resp = getResponse();
            if (expected != resp) {
                System.out.printf("Unexpected server response: %d. Expected: %d\n", resp, expected);
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
