package payment.system.app.exception;

public class WalletCreationException
        extends RuntimeException {

    public WalletCreationException(String message) {

        super(message);
    }

    public WalletCreationException(
            String message,
            Throwable ex) {

        super(message, ex);
    }
}