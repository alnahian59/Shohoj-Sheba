package bd.shohojsheba.request;

/** Used for both missing references and invalid tracking codes. */
public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException() {
        super("Request was not found.");
    }
}
