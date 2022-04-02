/*
 * This file is part of toothpick, licensed under the Apache License, Version 2.0 (the "License").
 *
 * Copyright (c) 2022-present lusory contributors
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public Class<?> typeToClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return (Class<?>) type;
    }
}
