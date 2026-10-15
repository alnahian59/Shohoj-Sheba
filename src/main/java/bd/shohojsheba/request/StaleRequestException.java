package bd.shohojsheba.request;

public class StaleRequestException extends RuntimeException {
    public StaleRequestException() {
        super("This request changed. Reload it before updating.");
    }
}
