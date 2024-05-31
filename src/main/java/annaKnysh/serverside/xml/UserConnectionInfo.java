package annaKnysh.serverside.xml;

import jakarta.xml.bind.annotation.*;

@SuppressWarnings("unused")
@XmlRootElement
public class UserConnectionInfo {
    private String username;
    private int port;

    public UserConnectionInfo() {
    }

    public UserConnectionInfo(String username, int port) {
        this.username = username;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
