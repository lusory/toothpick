package me.lusory.toothpick.util;

import lombok.Data;

@Data(staticConstructor = "of")
public class Tuple3<A, B, C> {
    private final A a;
    private final B b;
    private final C c;
}
