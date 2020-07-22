package IO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {

    protected Socket socket;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected String username;

    public SocketThread(Socket socket) throws IOException {
        this.socket = socket;
        this.username = null;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public void sendRequest(String req) throws IOException {
        os.writeUTF(req);
    }

    protected void whaitForInputData(long timeout) throws IOException {
        //todo: timeout
        while (is.available() == 0) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        try {
            is.close();
            os.close();
            socket.close();
            System.out.println("Interrupted");
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }
}
