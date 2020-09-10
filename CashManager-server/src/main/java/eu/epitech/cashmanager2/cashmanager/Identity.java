package eu.epitech.cashmanager2.cashmanager;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to carry information about the current user
 */
public class Identity {
    private static Map<String, Identity> Sessions = new HashMap<>();

    private String pass;
    private boolean valid;

    /**
     * Returns a boolean stating if the Identity is valid
     * @return the validity of the current Identity
     */
    public boolean valid() {
        return this.valid;
    }

    /**
     * Generates an Identity based on the specified password
     * @param password Password to use to authenticate a client
     * @param session Session for this identity
     */
    public Identity(String password, WebSocketSession session) {
        pass = password;
        valid = true;
        Sessions.put(session.getId(), this);
    }

    /**
     * Returns the Identity associated with the specific session
     * @param session current session
     * @return the associated Identity if applicable
     */
    public static Identity getId(WebSocketSession session) {
        return Sessions.getOrDefault(session.getId(), null);
    }

    /**
     * Destroys the identity associated with a session
     * @param session the session to destroy
     */
    public static void clean(WebSocketSession session) {
        Sessions.remove(session.getId());
    }

    @Override
    public int hashCode() {
        return pass.hashCode();
    }
}
