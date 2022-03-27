package me.lusory.toothpick.exceptions;

public class UnsatisfiedDependencyException extends RuntimeException {
    public UnsatisfiedDependencyException() {
        super();
    }

    public UnsatisfiedDependencyException(String message) {
        super(message);
    }
}
