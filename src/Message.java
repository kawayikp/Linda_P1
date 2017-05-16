
import java.io.Serializable;
import java.util.Map;

abstract class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    protected MessageType mType;
    protected final String IP;
    protected final Integer port;
    protected int ID;
    protected String hostName;
    protected ResponseType rType;

    public Message(String IP, Integer port) {
        this.IP = IP;
        this.port = port;
        this.mType = MessageType.DEFAULT;
        this.rType = ResponseType.DEFAULT;
    }

    public MessageType getType() {
        return mType;
    }

    public String getIP() {
        return IP;
    }

    public Integer getPort() {
        return port;
    }
    
    public int getID() {
        return ID;
    }

    public String getHostName() {
        return hostName;
    }

    public ResponseType getResponse() {
        return rType;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setResponse(ResponseType rType) {
        this.rType = rType;
    }

    public String toString() {
        return "MessageType = " + mType + " IP = " + IP + " port = " + port + " response = " + rType;
    }
}

class ASKNAMEMessage extends Message {
    private static final long serialVersionUID = 1L;

    public ASKNAMEMessage(String IP, Integer port, String hostName) {
        super(IP, port);
        this.mType = MessageType.ASKNAME;
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String toString() {
        String s = super.toString();
        return s + " HostName = " + hostName;
    }
}

class ADDMessage extends Message {
    private static final long serialVersionUID = 1L;
    private Map<Integer, Host> nets; 

    public ADDMessage(String IP, Integer port, Integer ID, String hostName) {
        super(IP, port);
        this.mType = MessageType.ADD;
        this.ID = ID;
        this.hostName = hostName;
    }

    public int getID() {
        return ID;
    }

    public Map<Integer, Host> getNets() {
        return nets;
    }

    public void setNets(Map<Integer, Host> nets) {
        this.nets = nets;
    }

    public String toString() {
        String s = super.toString();
        return s + " ID = " + ID + " nets = " + nets;
    }
}

class OUTMessage extends Message {
    private static final long serialVersionUID = 1L;
    Tuple t;

    public OUTMessage(String IP, Integer port, Tuple t, Integer ID, String hostName) {
        super(IP, port);
        this.mType = MessageType.OUT;
        this.t = t;
        this.ID = ID;
        this.hostName = hostName;
    }

    public Tuple getTuple() {
        return t;
    }

    public String toString() {
        String s = super.toString();
        return s + " tuple = " + t;
    }
}

class RDMessage extends Message {
    private static final long serialVersionUID = 1L;
    Tuple t;

    public RDMessage(String IP, Integer port, MessageType mType, Tuple t, Integer ID, String hostName) {
        super(IP, port);
        this.mType = mType;
        this.t = t;
        this.ID = ID;
        this.hostName = hostName;
    }
    
    public Tuple getTuple() {
        return t;
    }

    public void setTuple(Tuple t) {
        this.t = t;
    }

    public String toString() {
        String s = super.toString();
        return s + " tuple = " + t;
    }
}

class INMessage extends Message {
    private static final long serialVersionUID = 1L;
    Tuple t;

    public INMessage(String IP, Integer port, MessageType mType, Tuple t, Integer ID, String hostName) {
        super(IP, port);
        this.mType = mType;
        this.t = t;
        this.ID = ID;
        this.hostName = hostName;
    }

    public Tuple getTuple() {
        return t;
    }

    public void setTuple(Tuple t) {
        this.t = t;
    }

    public String toString() {
        String s = super.toString();
        return s + " tuple = " + t;
    }
}

enum MessageType {
    DEFAULT, ADD, REMOVE, ASKNAME, OUT, IN, INBROADCAST, RD, RDBROADCAST, RESPONSE
}

enum ResponseType {
    DEFAULT, SUCCESS, ERROR,
}