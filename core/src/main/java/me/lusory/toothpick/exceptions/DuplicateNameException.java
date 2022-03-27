package me.lusory.toothpick.exceptions;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException() {
        super();
    }

    public DuplicateNameException(String message) {
        super(message);
    }
}
