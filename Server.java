import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    private ServerSocket ss;
    private Socket s;
    private ObjectInputStream in;

    public Server(int port) throws IOException {
        this.ss = new ServerSocket(port);
        this.ss.setSoTimeout(2000);
        this.ss.setReuseAddress(true);
    }

    public void connect() throws IOException {
        this.s = ss.accept();
        this.in = new ObjectInputStream(s.getInputStream());
    }

    public void close() throws IOException {
        this.ss.close();
    }

    public Socket getS() {
        return s;
    }

    public ObjectInputStream getIn() {
        return in;
    }

}
