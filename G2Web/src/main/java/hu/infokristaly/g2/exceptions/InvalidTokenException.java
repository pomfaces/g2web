package hu.infokristaly.g2.exceptions;

public class InvalidTokenException extends Exception {

    private static final long serialVersionUID = 5788832076232517695L;

    public InvalidTokenException(String msg) {
        super(msg);
    }
}
