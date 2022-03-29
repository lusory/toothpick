package me.lusory.toothpick.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.*;
import java.util.Arrays;

@UtilityClass
public class Reflect {
    public Method[] getMethods(Class<?> type) {
        Method[] methods = new Method[0];

        Class<?> current = type;
        do {
            methods = concatArray(methods, current.getDeclaredMethods());

            current = current.getSuperclass();
        } while (current != Object.class);

        return methods;
    }

    public Constructor<?>[] getConstructors(Class<?> type) {
        Constructor<?>[] constructors = new Constructor[0];

        Class<?> current = type;
        do {
            constructors = concatArray(constructors, current.getDeclaredConstructors());

            current = current.getSuperclass();
        } while (current != Object.class);

        return constructors;
    }

    public boolean isTypeEqual(Type left, Type right) {
        if (left instanceof Class<?> && right instanceof Class<?>) {
            return ((Class<?>) left).isAssignableFrom((Class<?>) right);
        } else if (left instanceof Class<?> && right instanceof ParameterizedType) {
            return ((Class<?>) left).isAssignableFrom((Class<?>) ((ParameterizedType) right).getRawType());
        } else if (left instanceof ParameterizedType && right instanceof Class<?>) {
            return ((Class<?>) ((ParameterizedType) left).getRawType()).isAssignableFrom((Class<?>) right);
        } else if (left instanceof ParameterizedType && right instanceof ParameterizedType) {
            final ParameterizedType left0 = (ParameterizedType) left;
            final ParameterizedType right0 = (ParameterizedType) right;

            if (!((Class<?>) left0.getRawType()).isAssignableFrom((Class<?>) right0.getRawType())) {
                return false;
            }
            final Type[] left0Args = left0.getActualTypeArguments();
            final Type[] right0Args = right0.getActualTypeArguments();
            if (left0Args.length != right0Args.length) {
                return false;
            }

            for (int i = 0; i < left0Args.length; i++) {
                if (!isTypeEqual(left0Args[i], right0Args[i])) {
                    return false;
                }
            }

            return true;
        }
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public <T> T[] concatArray(T[] a, T[] b) {
        final int aLen = a.length;
        final int bLen = b.length;

        final T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
