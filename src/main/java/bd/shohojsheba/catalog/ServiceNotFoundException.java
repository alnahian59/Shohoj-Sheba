package bd.shohojsheba.catalog;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException() {
        super("Service is not available.");
    }
}
