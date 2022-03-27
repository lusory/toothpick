package me.lusory.toothpick;

import java.util.Arrays;
import java.util.Collection;

public interface Toothpick {
    static Toothpick of(Class<?>... classes) {
        return of(Arrays.asList(classes));
    }

    static Toothpick of(Collection<Class<?>> classes) {
        return new ToothpickImpl(classes);
    }
}
