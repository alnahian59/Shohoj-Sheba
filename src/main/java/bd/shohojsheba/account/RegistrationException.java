package bd.shohojsheba.account;

public class RegistrationException extends RuntimeException {
    private final String messageEn;

    public RegistrationException(String messageBn, String messageEn) {
        super(messageBn);
        this.messageEn = messageEn;
    }

    public String getMessageEn() {
        return messageEn;
    }
}
