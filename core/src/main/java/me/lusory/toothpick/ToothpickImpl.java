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

package me.lusory.toothpick;

import me.lusory.toothpick.annotations.Autowired;
import me.lusory.toothpick.annotations.Component;
import me.lusory.toothpick.annotations.Named;
import me.lusory.toothpick.exceptions.*;
import me.lusory.toothpick.util.Assertions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

class ToothpickImpl implements Toothpick {
    private final Map<String, Object> instances = new HashMap<>();
    private final Set<Class<?>> classes;

    protected ToothpickImpl(Collection<Class<?>> classes) {
        this.classes = new HashSet<>(classes);

        for (final Class<?> clazz : classes) {
            if (isClassInstantiated(clazz)) {
                continue;
            }

            doAutowire(clazz, new ArrayList<>());
        }
    }

    private boolean isClassInstantiated(Class<?> clazz) {
        return instances.values().stream().anyMatch(e -> clazz.isAssignableFrom(e.getClass()));
    }

    private boolean hasClass(Class<?> clazz) {
        return classes.stream().anyMatch(clazz::isAssignableFrom);
    }

    private long getCount(Class<?> clazz) {
        return instances.values().stream()
                .filter(e -> e.getClass() == clazz)
                .count();
    }

    private String[] resolveNames(Class<?> clazz) {
        String[] names = null;
        if (clazz.isAnnotationPresent(Component.class)) {
            names = clazz.getAnnotation(Component.class).value();
        }
        if (names == null || names.length == 0) {
            names = new String[] { clazz.getSimpleName() + (getCount(clazz) + 1) };
        }
        return names;
    }

    private Constructor<?> resolveAutowiringConstructor(Class<?> clazz) {
        for (final Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (ctor.isAnnotationPresent(Autowired.class)) {
                return ctor;
            }
        }
        return null;
    }

    private Object doAutowire(Class<?> clazz, List<Class<?>> dependencyStack) {
        final Constructor<?> ctor = resolveAutowiringConstructor(clazz);
        Constructor<?> noArgCtor = null;
        try {
            noArgCtor = clazz.getConstructor();
        } catch (NoSuchMethodException ignored) {
            // ignored
        }

        if (ctor == null && noArgCtor != null) {
            try {
                return noArgCtor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeInstantiationException(e);
            }
        } else if (ctor == null) {
            return null;
        }

        ctor.setAccessible(true);

        dependencyStack.add(clazz);

        // resolve dependencies
        final List<Object> paramInstances = new ArrayList<>();
        for (final Parameter param : ctor.getParameters()) {
            if (!hasClass(param.getType())) {
                throw new UnsatisfiedDependencyException(param.getType().getName());
            }

            if (dependencyStack.contains(param.getType())) {
                throw new CyclicDependencyException(
                        "Cyclic dependency while resolving parameter " + param.getType().getName()
                                + " of constructor of class " + clazz.getName() + ", dependency stack: ["
                                + dependencyStack.stream().map(Class::getName).collect(Collectors.joining(", ")) + "]"
                );
            }

            if (param.isAnnotationPresent(Named.class)) {
                final Named named = param.getAnnotation(Named.class);
                // resolve dependency by its name
                final Object instance = instances.get(named.value());
                if (instance != null && param.getType().isAssignableFrom(instance.getClass())) {
                    paramInstances.add(instance);
                } else {
                    paramInstances.add(
                            Assertions.throwIfNull(doAutowire(param.getType(), dependencyStack), NoAutowiringConstructorFound.class)
                    ); // not sure about this one, check later
                }
            } else {
                paramInstances.add(
                        Assertions.throwIfNull(doAutowire(param.getType(), dependencyStack), NoAutowiringConstructorFound.class)
                ); // not sure about this one, check later
            }
        }

        Object instance;
        try {
            instance = ctor.newInstance(paramInstances.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeInstantiationException(e);
        }

        for (final String name : resolveNames(clazz)) {
            if (instances.put(name, instance) != null) {
                throw new DuplicateNameException(name);
            }
        }
        return instance;
    }
}
