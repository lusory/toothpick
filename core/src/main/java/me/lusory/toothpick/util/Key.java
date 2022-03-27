package me.lusory.toothpick.util;

import lombok.Data;

import javax.inject.Named;
import java.lang.annotation.Annotation;

@Data(staticConstructor = "of")
public class Key<T> {
    private final String name;
    private final Class<T> type;
    private final Class<? extends Annotation> qualifier;

    public static <T> Key<T> of(Class<T> type) {
        return new Key<>(null, type, null);
    }

    public static <T> Key<T> of(Class<T> type, Class<? extends Annotation> qualifier) {
        return new Key<>(null, type, qualifier);
    }

    public static <T> Key<T> of(Class<T> type, String name) {
        return new Key<>(name, type, Named.class);
    }

    public static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        if (qualifier == null) {
            return Key.of(type);
        }
        return qualifier.annotationType() == Named.class
                ? Key.of(type, ((Named) qualifier).value())
                : Key.of(type, qualifier.annotationType());
    }
}
