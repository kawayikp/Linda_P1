
import java.net.*;
import java.io.*;
public class Server implements Runnable{
    static String IP;
    static int port;
    static ServerSocket serverSocket;


    public Server() {
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        port = serverSocket.getLocalPort();
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

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
