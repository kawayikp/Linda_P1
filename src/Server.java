
import java.net.*;
import java.io.*;
public class Server implements Runnable{
    static String IP;
    static int port;
    static ServerSocket serverSocket;


    public Server() throws IOException {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        IP = InetAddress.getLocalHost().getHostAddress();
        System.out.println(P1.hostName + " at " + IP + " : " + port); 
    }

    @Override
    public void run() {
        while (true) {
            try {
                new Thread(new ServerThread(serverSocket.accept())).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
