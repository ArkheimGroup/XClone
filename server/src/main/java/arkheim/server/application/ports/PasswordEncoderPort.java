package arkheim.server.application.ports;

public interface PasswordEncoderPort {

    /**
     * @return hashed password
     * */
    String encode(String rawPassword);

    /**
     * @return true if rawPassword hash is the same as hashPassword, OW false
     * */
    boolean matches(String rawPassword, String hashPassword);
}
