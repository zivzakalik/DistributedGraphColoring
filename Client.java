import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client {
    Socket s;
    ObjectOutputStream out;
    int port;

    public Client(int port) throws IOException {
        this.port = port;
        this.s = new Socket("localhost", port);
        this.out = new ObjectOutputStream(s.getOutputStream());
    }

    public void sendMess(Message mess) throws IOException {
        out.writeObject(mess);
    }

    public void close() throws IOException {
        this.s.close();
    }
}
