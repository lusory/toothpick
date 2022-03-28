package me.lusory.toothpick;

import me.lusory.toothpick.annotations.Provides;
import me.lusory.toothpick.exceptions.InjectorException;
import me.lusory.toothpick.util.Key;
import me.lusory.toothpick.util.Reflect;
import me.lusory.toothpick.util.Tuple3;

import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InjectorImpl implements Injector {
    private final Map<Key<?>, Provider<?>> providers = new ConcurrentHashMap<>();
    private final Map<Key<?>, Object> singletons = new ConcurrentHashMap<>();

    protected InjectorImpl(Collection<Object> modules) {
        populate(modules);
    }

    private void populate(Collection<Object> modules) {
        // key, module, method
        final List<Tuple3<Key<?>, Object, Method>> providers0 = new ArrayList<>();
        final List<Tuple3<Key<?>, Object, Method>> singletons0 = new ArrayList<>();

        // discovery
        for (Object module : modules) {
            if (module instanceof Class<?>) {
                try {
                    module = ((Class<?>) module).getConstructor().newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
                    throw new InjectorException("Could not instantiate module " + ((Class<?>) module).getName());
                }
            }

            for (final Method method : Reflect.getMethods(module.getClass())) {
                if (method.isAnnotationPresent(Provides.class)) {
                    final Tuple3<Key<?>, Object, Method> tuple = Tuple3.of(Key.of(method.getReturnType(), getQualifier(method.getAnnotations())), module, method);
                    ((method.isAnnotationPresent(Singleton.class) || method.getReturnType().isAnnotationPresent(Singleton.class)) ? singletons0 : providers0).add(tuple);
                }
            }
        }
    }

    private Annotation getQualifier(Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }
}
