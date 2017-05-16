
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ServerThread implements Runnable {
    private Socket socket;
    private Message message;

    private ObjectOutputStream out;
    // private Object outObject;
    private ObjectInputStream in;
    // private Object inObject;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

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

            out.writeObject(message);
            out.flush();
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void askNameHelper() {
        ASKNAMEMessage am = (ASKNAMEMessage) message;
        if (!am.getHostName().equals(P1.hostName)) {
            am.setResponse(ResponseType.ERROR);
            System.out.println("ERROR. Host name doesn't match with " + P1.hostName);
//            System.out.println("ServerThread[ASKNAME]: FINISH");
        } else {
            am.setResponse(ResponseType.SUCCESS);
//            System.out.println("ServerThread[ASKNAME]: Name is correct on " + P1.hostName);
//            System.out.println("ServerThread[ASKNAME]: SUCCESS");
        }
    }

    private void addHelper() {
        ADDMessage am = (ADDMessage) message;
        P1.ID = am.getID();
        P1.nets = am.getNets();
        P1.IDS = am.getNets().size();
        InputOutputController.serialize(P1.nets, P1.path + P1.netsFile);
        am.setResponse(ResponseType.SUCCESS);
        System.out.println("Joined Linda");
        System.out.println("Nets = " + P1.nets);
//        System.out.println("ServerThread[ADD]: SUCCESS");
    }

    private void outHelper() {
        OUTMessage om = (OUTMessage) message;
        Tuple t = om.getTuple();
        synchronized (P1.tuples) {
            P1.tuples.put(t, P1.tuples.getOrDefault(t, 0) + 1);
            InputOutputController.serialize(P1.tuples, P1.path + P1.tuplesFile);
            om.setResponse(ResponseType.SUCCESS);
            System.out.println("Put tuple " + om.getTuple());
//            System.out.println("ServerThread[OUT]: SUCCESS");
            P1.tuples.notifyAll();
        }
    }

    private void rdHelper() throws InterruptedException {
        RDMessage rm = (RDMessage) message;
        Tuple t = rm.getTuple();

        if (P1.tuples.containsKey(t)) {
            rm.setResponse(ResponseType.SUCCESS);
            System.out.println("ServerThread[RD]: Read tuple ");
//            System.out.println("ServerThread[RD]: SUCCESS");
        } else {
//            System.out.println("ServerThread[RD]: Don't have the tuple now.");
//            System.out.println("ServerThread[RD]: FINISH");
        }
    }

    private void inHelper() throws InterruptedException {
        INMessage im = (INMessage) message;
        Tuple t = im.getTuple();
        synchronized (P1.tuples) {
            int value = P1.tuples.getOrDefault(t, 0);
            if (value != 0) {
                if (value == 1) {
                    P1.tuples.remove(t);
                } else {
                    P1.tuples.put(t, value - 1);
                }
                InputOutputController.serialize(P1.tuples, P1.path + P1.tuplesFile);
                im.setResponse(ResponseType.SUCCESS);
                System.out.println("Remove tuple " + t);
//                System.out.println("ServerThread[IN]: SUCCESS");
            } else {
//                System.out.println("ServerThread[IN]: Don't have the tuple now.");
//                System.out.println("ServerThread[IN]: FINISH");
            }
            P1.tuples.notifyAll();
        }
    }

    private void rdbroadcastHelper() throws InterruptedException {
        RDMessage rm = (RDMessage) message;
        Tuple other = rm.getTuple();
        
        for (Map.Entry<Tuple, Integer> entry : P1.tuples.entrySet()) {
            Tuple tuple = entry.getKey();
            if (tuple.typeMatch(other)) {
                rm.setTuple(tuple);
                rm.setResponse(ResponseType.SUCCESS);
                System.out.println("Read tuple = " + rm.getTuple());
//                System.out.println("ServerThread[RDBROADCAST]: SUCCESS");
                return;
            }
        }
        
//        System.out.println("ServerThread[RDBROADCAST]: Don't have the tuple now.");
//        System.out.println("ServerThread[RDBROADCAST]: FINISH");
    }
    
    private void inbroadcastHelper() throws InterruptedException {
        INMessage im = (INMessage) message;
        Tuple other = im.getTuple();
        
        for (Map.Entry<Tuple, Integer> entry : P1.tuples.entrySet()) {
            Tuple tuple = entry.getKey();
            if (tuple.typeMatch(other)) {
                im.setTuple(tuple);
                im.setResponse(ResponseType.SUCCESS);
                System.out.println("Read tuple = " + im.getTuple());
//                System.out.println("ServerThread[INBROADCAST]: SUCCESS");
                return;
            }
        }
        
//        System.out.println("ServerThread[IN]: Don't have the tuple now.");
//        System.out.println("ServerThread[IN]: FINISH");
    }
}
