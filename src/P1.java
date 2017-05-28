
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class P1 {
    static String hostName;
    static int IDS; 
    static int ID;          // from [0...IDS-1]

    private static Server server;

    static Map<Tuple, Integer> tuples;
    static Map<Integer, Host> nets;

    static String path;
    static String[] paths;
    static String netsFile;
    static String tuplesFile;

    public static void initialization(String hostName) {
        P1.hostName = hostName;
        ID = IDS++;

        server = new Server();
        new Thread(server).start();

        tuples = new HashMap<>();
        nets = new Hashtable<>();

        path = "/tmp/yliu3/linda/" + hostName + "/";
        paths = new String[] { "/tmp/yliu3/", "/tmp/yliu3/linda/", "/tmp/yliu3/linda/" + hostName + "/" };
        netsFile = "nets.txt";
        tuplesFile = "tuples.txt";

        nets.put(ID, new Host(hostName, Server.IP, Server.port)); // !!!

        InputOutputController.generateFile(paths, path, netsFile, tuplesFile);
        InputOutputController.serialize(nets, path + netsFile);
        InputOutputController.serialize(tuples, path + tuplesFile);
    }

    public static void main(String[] args) { // P1 host0
        P1.initialization(args[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("linda>");
            String commandLine = "";
            try {
                commandLine = br.readLine().trim();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (commandLine.length() == 0) {
                continue;
            } else if (commandLine.equals("t")) {
                System.out.println(P1.tuples);
                continue;
            } else if (commandLine.equals("n")) {
                System.out.println(P1.nets);
                continue;
            } else if (commandLine.equals("exit")) {
                System.exit(0);
            }

            List<Message> messageList = null;
            try {
                messageList = MessageBuilder.buildMessage(commandLine);
            } catch (Exception e) {
                System.out.println(" Please try again");
                //                e.printStackTrace();
                continue;
            }

            //            System.out.println("P1 : messageList = " + messageList);
            Client client = new Client(messageList);
            client.run(messageList);
        }
    }

    public static String info() {
        return "P1: IDS = " + P1.IDS + " ID = " + P1.ID + " nets = " + P1.nets + " tuples = " + P1.tuples;
    }
}
