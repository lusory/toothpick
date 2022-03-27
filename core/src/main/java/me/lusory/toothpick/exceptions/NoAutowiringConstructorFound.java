package me.lusory.toothpick.exceptions;

public class NoAutowiringConstructorFound extends RuntimeException {
    public NoAutowiringConstructorFound() {
        super();
    }

    public NoAutowiringConstructorFound(String message) {
        super(message);
    }
}
