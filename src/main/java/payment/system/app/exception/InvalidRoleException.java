package payment.system.app.exception;

public class InvalidRoleException
        extends RuntimeException {

    public InvalidRoleException(String message) {

        super(message);
    }
}