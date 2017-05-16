
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MessageBuilder {
    private static final Pattern IPV4 = Pattern.compile("(([1-9]|1\\d|2[0-4])?\\d|25[0-5])(\\.(([1-9]|1\\d|2[0-4])?\\d|25[0-5])){3}");
    private final static String TYPEMATCH_INTEGER_NAME = "class java.lang.Integer";
    private final static String TYPEMATCH_FLOAT_NAME = "class java.lang.Float";
    private final static String TYPEMATCH_STRING_NAME = "class java.lang.String";

    // dispatch the request to special message builder
    public static List<Message> buildMessage(String s) throws Exception {
        /*
         * add(h2, ip2, port2) (h3, ip3, port3)
         * out("abc", 3)
         * in("abc", ?i:int)
         * rd()
         * input is trimed
         * 
         */

        if (s.substring(0, 3).equalsIgnoreCase("add")) {
            return buildAskNameMessage(s.substring(3).trim());
        } else if (s.substring(0, 3).equalsIgnoreCase("out")) {
            return buildOutMessage(s.substring(3).trim());
        } else if (s.substring(0, 2).equalsIgnoreCase("rd")) {
            return buildRdMessage(s.substring(2).trim());
        } else if (s.substring(0, 2).equalsIgnoreCase("in")) {
            return buildInMessage(s.substring(2).trim());
        } else {
            System.out.print("Command is wrong.");
            throw new Exception();
        }
    }

    private static List<Message> buildAskNameMessage(String s) throws Exception {
        List<Message> list = new ArrayList<>();
        /* 
         * input (h2, ip2, port2) (h3, ip3, port3)
         * 1. split by ")", get "(h2, ip2, port2"
         * 2. remove "(", get "h2, ip2, port2"
         * 3. split by ",", get ["h2", "ip2", "port2"]
         * 4. check IP and port
         */
        String[] ss = s.split("\\)"); // (host_1, 172.21.89.108, 57577    ((host_1, 172.21.89.108, 12345
        for (int i = 0; i < ss.length; i++) {
            String[] temp = ss[i].trim().split(","); //(host_1    172.21.89.108   57577
            if (temp.length != 3) {
                System.out.print("Add host format is wrong.");
                throw new Exception();
            }
            String name = temp[0].trim().substring(1); 
            String IP = getIP(temp[1].trim()); 
            Integer port = getPort(temp[2].trim()); 
            list.add(new ASKNAMEMessage(IP, port, name));
        }
        return list;
    }

    private static List<Message> buildOutMessage(String s) throws Exception { 
        //("abc", +3, -7.5, -7.5f, 2e3)
        Tuple tuple = tupleBuilder(s);
        int ID = tupleToHost(tuple);
        Host host = P1.nets.get(ID);
        List<Message> messageList = new ArrayList<>();
        messageList.add(new OUTMessage(host.getIP(), host.getPort(), tuple, ID, host.getHostName()));
        return messageList;
    }

    private static List<Message> buildRdMessage(String s) throws Exception {
        Tuple tuple = tupleBuilder(s);

        List<Message> messageList = new ArrayList<>();
        if (isTypeMatch(s)) {
            messageList = broadcastHelper(MessageType.RDBROADCAST, tuple);
        } else {
            int ID = tupleToHost(tuple);
            Host host = P1.nets.get(ID);
            messageList.add(new RDMessage(host.getIP(), host.getPort(), MessageType.RD, tuple, ID, host.getHostName()));
        }
        return messageList;
    }

    private static List<Message> buildInMessage(String s) throws Exception {
        Tuple tuple = tupleBuilder(s);

        List<Message> messageList = new ArrayList<>();
        if (isTypeMatch(s)) {
            messageList = broadcastHelper(MessageType.INBROADCAST, tuple);
        } else {
            int ID = tupleToHost(tuple);
            Host host = P1.nets.get(ID);
            messageList.add(new INMessage(host.getIP(), host.getPort(), MessageType.IN, tuple, ID, host.getHostName()));
        }
        return messageList;
    }

    private static List<Message> broadcastHelper(MessageType mType, Tuple tuple) {
        List<Message> messageList = new ArrayList<>();
        for (int i = 0; i < P1.IDS; i++) {
            Host host = P1.nets.get(i);
            switch (mType) {
            case RDBROADCAST:
                messageList.add(new RDMessage(host.getIP(), host.getPort(), MessageType.RDBROADCAST, tuple, i, host.getHostName()));
                break;
            case INBROADCAST:
                messageList.add(new INMessage(host.getIP(), host.getPort(), MessageType.INBROADCAST, tuple, i, host.getHostName()));
                break;
            default:
                System.out.println("SYSTEM ERROR");
                break;
            }
        }
        return messageList;
    }

    private static boolean isTypeMatch(String s) {
        // ("abc", +3, -7.5, -7.5f, 2e3)
        s = s.substring(1, s.length() - 1); 
        /*
         * input is 
         * case1: "abc", +3, -7.5, -7.5f, 2e3
         * case2: ?i:int, ?f:float, ?s:string
         * 
         * 1. split by "," , get field  [" ?i:int ", " 3"]
         * 2. check whether is type match
         */
        String[] ss = s.split(","); // [" ?i:int ", " 3"]
        for (int i = 0; i < ss.length; i++) {
            String temp = ss[i].trim(); // "abc"    +3  -7.5f   2e3
            // type match
            if (temp.charAt(0) == '?' && temp.indexOf(':') != -1) {
                return true;
            }
        }
        return false;
    }

    private static Tuple tupleBuilder(String s) throws Exception {
        /*
         * input : 
         * case1: ("abc", +3, -7.5, -7.5f, 2e3)
         * case2: ("abc", ?i:int, ?f:float, ?s:string)
         * 
         * 1.substring and split by ",", get ["abc" , +3, -7.5, -7.5f, 2e3]
         * 2.check each field
         *      case1: type match
         *      case2: String
         *      case3: float
         *      case4: int
         */

        if (s.length() < 3 || s.charAt(0) != '(' || s.charAt(s.length() - 1) != ')') {
            System.out.print("Tuple format is wrong. ");
            throw new Exception();
        }

        s = s.substring(1, s.length() - 1);         // "abc", +3, -7.5, -7.5f, 2e3
        String[] ss = s.split(",");                 // ["abc" , +3, -7.5, -7.5f, 2e3]
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < ss.length; i++) {
            String temp = ss[i].trim();             // ?i:int || "abc" || +3 || -7.5f || 2e3
            Object o = null;
            // type match
            if (temp.charAt(0) == '?') {
                int index = temp.indexOf(':');
                String name = temp.substring(1, index);
                String type = temp.substring(index + 1);
                if (type.equalsIgnoreCase("int")) {
                    type = TYPEMATCH_INTEGER_NAME;
                } else if (type.equalsIgnoreCase("float")) {
                    type = TYPEMATCH_FLOAT_NAME;
                } else if (type.equalsIgnoreCase("string")) {
                    type = TYPEMATCH_STRING_NAME;
                } else {
                    System.out.print("Type match format is wrong. ");
                    throw new Exception();
                }
                o = new ArrayList<String>(Arrays.asList(name, type));
                // type is string
            } else if (temp.charAt(0) == '"' && temp.charAt(temp.length() - 1) == '"') {
                o = new String(temp.substring(1, temp.length() - 1));
                // type is float
            } else if (temp.indexOf('.') != -1 || temp.indexOf('e') != -1 || temp.indexOf('E') != -1) {
                try {
                    o = Float.valueOf(temp);
                } catch (Exception e) {
                    System.out.print("Float format is wrong. ");
                    throw new Exception();
                }

                if ((Float) o == Float.POSITIVE_INFINITY) {
                    System.out.print("Float is overflow. ");
                    throw new Exception();
                }
                // type is int
            } else {
                try {
                    o = Integer.valueOf(temp);
                } catch (Exception e) {
                    System.out.print("Tuple format is wrong. ");
                    throw new Exception();
                }
            }
            list.add(o);
        }
        Tuple t = new Tuple(list);
        return t;
    }

    private static String getIP(String s) throws Exception {
        if (IPV4.matcher(s).matches()) {
            return s;
        } else {
            System.out.print("IP is wrong.");
            throw new Exception();
        }
    }

    private static Integer getPort(String s) throws Exception {
        Integer port = Integer.parseInt(s);
        if (port <= 1023) {
            System.out.print("Port number is wrong. ");
            throw new Exception();
        }
        return port;
    }

    private static int tupleToHost(Tuple tuple) throws NoSuchAlgorithmException {
        byte[] digest = Tuple.MD5Sum(tuple);
        BigInteger bigI = new BigInteger(digest);
        BigInteger bigImod = new BigInteger(P1.IDS + "");
        return Integer.parseInt(bigI.mod(bigImod).toString());
    }
}