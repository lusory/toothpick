package me.lusory.toothpick;

import me.lusory.toothpick.annotations.Autowired;
import me.lusory.toothpick.annotations.Component;
import me.lusory.toothpick.annotations.Named;
import me.lusory.toothpick.exceptions.DuplicateNameException;
import me.lusory.toothpick.exceptions.NoAutowiringConstructorFound;
import me.lusory.toothpick.exceptions.RuntimeInstantiationException;
import me.lusory.toothpick.exceptions.UnsatisfiedDependencyException;
import me.lusory.toothpick.util.Assertions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

class ToothpickImpl implements Toothpick {
    private final Map<String, Object> instances = new HashMap<>();
    private final Set<Class<?>> classes;

    protected ToothpickImpl(Collection<Class<?>> classes) {
        this.classes = new HashSet<>(classes);

        for (final Class<?> clazz : classes) {
            if (isClassInstantiated(clazz)) {
                continue;
            }

            doAutowire(clazz);
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
        return clazz.isAnnotationPresent(Component.class)
                ? clazz.getAnnotation(Component.class).value()
                : new String[] { clazz.getSimpleName() + (getCount(clazz) + 1) };
    }

    private Constructor<?> resolveAutowiringConstructor(Class<?> clazz) {
        for (final Constructor<?> ctor : clazz.getDeclaredConstructors()) { // fuck super class constructors
            if (ctor.isAnnotationPresent(Autowired.class)) {
                return ctor;
            }
        }
        return null;
    }

    private Object doAutowire(Class<?> clazz) {
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

        // resolve dependencies
        final List<Object> paramInstances = new ArrayList<>();
        for (final Parameter param : ctor.getParameters()) {
            if (!hasClass(param.getType())) {
                throw new UnsatisfiedDependencyException(param.getType().getName());
            }

            if (param.isAnnotationPresent(Named.class)) {
                final Named named = param.getAnnotation(Named.class);
                // resolve dependency by its name
                final Object instance = instances.get(named.value());
                if (instance != null && param.getType().isAssignableFrom(instance.getClass())) {
                    paramInstances.add(instance);
                } else {
                    paramInstances.add(Assertions.throwIfNull(doAutowire(param.getType()), NoAutowiringConstructorFound.class)); // not sure about this one, check later
                }
            } else {
                paramInstances.add(Assertions.throwIfNull(doAutowire(param.getType()), NoAutowiringConstructorFound.class)); // not sure about this one, check later
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
