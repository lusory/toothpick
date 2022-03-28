package me.lusory.toothpick.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.util.Arrays;

@UtilityClass
public class Reflect {
    public Method[] getMethods(Class<?> type) {
        Method[] methods = new Method[0];

        Class<?> current = type;
        do {
            final Method[] newMethods = current.getDeclaredMethods();
            methods = Arrays.copyOf(methods, methods.length + newMethods.length);
            System.arraycopy(newMethods, 0, methods, methods.length, newMethods.length);

            current = current.getSuperclass();
        } while (current != Object.class);

        return methods;
    }
}
