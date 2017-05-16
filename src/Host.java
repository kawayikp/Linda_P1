
import java.io.Serializable;

class Host implements Serializable {
    private static final long serialVersionUID = 1L;
    String hostName;
    String IP;
    Integer port;

    public Host(String hostName, String IP, Integer port) {
        this.hostName = hostName;
        this.IP = IP;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public String getIP() {
        return IP;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this.getClass() != o.getClass()) {
            return false;
        }

        Host other = (Host) o;
        return this.IP.equals(other.IP) && this.port.equals(other.port);
    }
    
    @Override
    public int hashCode() {
        return IP.hashCode() + port;     //  IP + port
    }
    
    @Override
    public String toString() {
        return hostName;
    }
}
