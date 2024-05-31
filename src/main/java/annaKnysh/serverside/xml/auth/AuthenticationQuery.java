package annaKnysh.serverside.xml.auth;
import jakarta.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("unused")
@XmlRootElement
public class AuthenticationQuery {
    private String username;

    public AuthenticationQuery() {
    }

    public AuthenticationQuery(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

}
