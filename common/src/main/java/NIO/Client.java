package NIO;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private int userID;
    private final String clientFilesPath;

    public Client(int id) {
        userID = id;
        clientFilesPath = "./common/src/resources/user" + userID;
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            Path path = Paths.get(clientFilesPath);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch(
        IOException e)

        {
            e.printStackTrace();
        }

        File dir = new File(clientFilesPath);
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(String tag, String parameter) {
        if (tag.equals("./download")) {
            try {
                os.write(String.format("%s %s %d\0", tag, parameter, userID).getBytes("UTF16"));
                int response = is.read();

                if (response == 0) {
                    File file = new File(clientFilesPath + "/" + parameter);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    long len = is.readLong();
                    System.out.println(len);
                    byte[] buffer = new byte[1024];
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        if (len < 1024) {
                            int count = is.read(buffer);
                            fos.write(buffer, 0, count);
                        } else {
                            for (long i = 0; i < len / 1024; i++) {
                                int count = is.read(buffer);
                                fos.write(buffer, 0, count);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(() -> {
            Client c = new Client(1);
            c.sendCommand("./download", "1.txt");
            c.sendCommand("./download", "1.txt");
        }).start();

        new Thread(() -> {
            Client c = new Client(2);
            c.sendCommand("./download", "2.txt");
        }).start();

        new Thread(() -> {
            Client c = new Client(2);
            c.sendCommand("./download", "2.txt");
        }).start();

    }
}
