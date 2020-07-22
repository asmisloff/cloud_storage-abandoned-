package IO;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        testUpload("user1");
        testUpload("user2");
        testUpload("user3");

        Thread.sleep(10000);

        testDownload("user1");
        testDownload("user2");
        testDownload("user3");

        Thread.sleep(10000);

        testInfo("user1");
    }

    private static void testUpload(String username) {
        new Thread(() -> {
            try {
                ClientSocketThread client = new ClientSocketThread("localhost", 8189, username);
                client.start();
                System.out.println(client.username);
                if (client.login()) {
                    File file = new File("./common/344-002 --  Задняя стенка бара -- v04.dwg");
                    boolean success = client.uploadFile(file);
                    if (success) {
                        System.out.println("Success");
                        client.sendRequest("*DISCONNECT*");
                    }
                }
                client.interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void testDownload(String username) {
        new Thread(() -> {
            ClientSocketThread client = null;
            try {
                client = new ClientSocketThread("localhost", 8189, username);

                client.start();
                System.out.println(client.username);
                if (client.login()) {
                    boolean success = client.downloadFile("344-002 --  Задняя стенка бара -- v04.dwg");
                    if (success) {
                        System.out.println("Success");
                        client.sendRequest("*DISCONNECT*");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.interrupt();
        }).start();
    }

    private static void testInfo(String username) {
        new Thread(() -> {
            ClientSocketThread client = null;
            try {
                client = new ClientSocketThread("localhost", 8189, username);

                client.start();
                System.out.println(client.username);
                if (client.login()) {
                    String info = client.getFileInfo();
                    if (info != null) {
                        System.out.println(info);
                        client.sendRequest("*DISCONNECT*");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.interrupt();
        }).start();
    }
}
