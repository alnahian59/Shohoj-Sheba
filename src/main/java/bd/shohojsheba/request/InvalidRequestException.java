package bd.shohojsheba.request;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException() {
        super("Please check the submitted fields.");
    }
}
