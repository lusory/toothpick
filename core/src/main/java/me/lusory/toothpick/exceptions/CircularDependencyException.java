package me.lusory.toothpick.exceptions;

import lombok.experimental.StandardException;

@StandardException
public class CircularDependencyException extends RuntimeException {
}
