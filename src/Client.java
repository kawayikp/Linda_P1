import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Client {
    List<Message> messageSend;
    Queue<Message> messageReceive;

    public Client(List<Message> messageSend) {
        this.messageSend = messageSend;
        messageReceive = new LinkedList<>();
    }

    public void run(List<Message> messageSend) throws InterruptedException {
        MessageType type = messageSend.get(0).getType();
        switch (type) {
        case ASKNAME:
        case ADD:
        case OUT:
            sendUnblockingMessage(messageSend);
            break;
        case RD:
        case IN:
        case RDBROADCAST:
        case INBROADCAST:
            sendBlockingMessage(messageSend);
            break;
        default:
            break;
        }
    }

    private void sendUnblockingMessage(List<Message> messageSend) throws InterruptedException {
        int n = messageSend.size();
        Thread[] threads = new Thread[n];

        for (int i = 0; i < n; i++) {
            try {
                threads[i] = new Thread(new ClientThread(messageSend.get(i)));
            } catch (IOException e) {
                System.out.println("Connection is failed, please try again.");
                return;
                //               e.printStackTrace();
            }
        }

        for (Thread t : threads) {
            t.start();
            t.join();
        }
        //
        //        System.out.println("Client[]: " + n + " sent");
        //        System.out.println("Client[]: " + messageReceive.size() + " recieved");

        switch (messageReceive.peek().getType()) {
        case ASKNAME:
            askNameHelper();
            break;
        case ADD:
            addHelper();
            break;
        case OUT:
            outHelper();
            break;
        default:
            break;
        }
    }

    private void sendBlockingMessage(List<Message> messageSend) throws InterruptedException {
        int n = messageSend.size();
        Thread[] threads = new Thread[n];
        boolean finished = false;

        while (!finished) {
            for (int i = 0; i < n; i++) {
                try {
                    threads[i] = new Thread(new ClientThread(messageSend.get(i)));
                } catch (IOException e) {
                    System.out.println("System ERROR, Connection is failed. ");
                    return;
                    //                    e.printStackTrace();
                }
            }

            for (int i = 0; i < n; i++) {
                threads[i].start();
                threads[i].join();
            }

            //            System.out.println("Client[]: " + n + " sent");
            //            System.out.println("Client[]: " + messageReceive.size() + " recieved");

            synchronized (messageReceive) {
                while (!messageReceive.isEmpty()) {
                    if (messageReceive.peek().getResponse() == ResponseType.SUCCESS) {
                        finished = true;
                        break;
                    } else {
                        messageReceive.poll();
                    }
                }
                messageReceive.notifyAll();
            }
        }

        switch (messageReceive.peek().getType()) {
        case RD:
            rdHelper();
            break;
        case IN:
            inHelper();
            break;
        case RDBROADCAST:
            rdbroadcastHelper();
            break;
        case INBROADCAST:
            inbroadcastHelper();
            break;
        default:
            break;
        }
    }

    // check hosts information, if no duplicated host name, update nets of all hosts, otherwise cancel the request and input again
    private void askNameHelper() throws InterruptedException {
        List<Message> messageSend = new ArrayList<>();
        Set<String> nameSet = new HashSet<>();
        nameSet.add(P1.hostName);

        while (!messageReceive.isEmpty()) {
            ASKNAMEMessage am = (ASKNAMEMessage) (messageReceive.poll());
            String name = am.getHostName();
            if (am.getResponse() == ResponseType.ERROR) {   // host name is wrong
                System.out.println("Host name is wrong, please try again.");
                restoreMaster();
                return;
            }
            if (!nameSet.add(name)) {                       // duplicated host name
                System.out.println("Duplicat host name, please try again.");
                restoreMaster();
                return;
            }
            String IP = am.getIP();
            int port = am.getPort();
            int id = P1.IDS++;
            String hostName = am.getHostName();
            Message m = new ADDMessage(am.getIP(), am.getPort(), id, hostName);
            messageSend.add(m);
            P1.nets.put(id, new Host(name, IP, port));
        }
        InputOutputController.serialize(P1.nets, P1.path + P1.netsFile);
        for (Message m : messageSend) {
            ((ADDMessage) m).setNets(P1.nets);
        }
        //        System.out.println("Client[ASKNAME]: SUCCESS");
        run(messageSend);
    }

    private void restoreMaster() {
        P1.IDS = 1;
        P1.nets.clear();
        P1.nets.put(P1.ID, new Host(P1.hostName, Server.IP, Server.port));
    }

    private void addHelper() {
        System.out.println("Add hosts");
        System.out.println("Nets = " + P1.nets);
    }

    private void outHelper() {
        OUTMessage om = (OUTMessage) (messageReceive.poll());
        System.out.println("Put tuple on " + om.getHostName());
    }

    private void rdHelper() {
        RDMessage rd = (RDMessage) (messageReceive.poll());
        System.out.println("Read tuple on " + rd.getHostName());
    }

    private void inHelper() {
        INMessage im = (INMessage) (messageReceive.poll());
        System.out.println("Get tuple = " + im.getTuple() + " on " + im.getHostName());
    }

    private void rdbroadcastHelper() {
        RDMessage rm = (RDMessage) (messageReceive.poll()); 
        System.out.println("Read tuple = " + rm.getTuple() + " on " + rm.getHostName());
    }

    private void inbroadcastHelper() throws InterruptedException {
        INMessage im = (INMessage) (messageReceive.poll()); 
        List<Message> list = new ArrayList<>();
        Integer ID = im.getID();
        Host host = P1.nets.get(ID);
        INMessage m = new INMessage(host.getIP(), host.getPort(), MessageType.IN, im.getTuple(), ID, host.getHostName());
        list.add(m);
        messageReceive.clear();
        run(list);
    }

    class ClientThread implements Runnable {
        private Socket socket;
        Message message;

        private ObjectOutputStream out;
        //        private Object outObject;
        private ObjectInputStream in;
        //        private Object inObject;

        public ClientThread(Message message) throws IOException {
            socket = new Socket(message.IP, message.port);
            this.message = message;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                out.writeObject(message);
                out.flush();

                message = (Message) in.readObject();

                switch (message.getType()) {
                case ASKNAME:
                    askNameHelper();
                    break;
                case ADD:
                    addHelper();
                    break;
                case OUT:
                    outHelper();
                    break;
                case RD:
                    rdHelper();
                    break;
                case IN:
                    inHelper();
                    break;
                case RDBROADCAST:
                    rdbroadcastHelper();
                    break;
                case INBROADCAST:
                    inbroadcastHelper();
                    break;
                default:
                    break;
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void askNameHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void addHelper() { 
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void outHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void rdHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void inHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void rdbroadcastHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }

        private void inbroadcastHelper() {
            synchronized (messageReceive) {
                messageReceive.add(message);
                messageReceive.notifyAll();
            }
        }
    }
}
