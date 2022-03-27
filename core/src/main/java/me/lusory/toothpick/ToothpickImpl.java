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

import me.lusory.toothpick.annotations.Provides;
import me.lusory.toothpick.exceptions.CircularDependencyException;
import me.lusory.toothpick.exceptions.DuplicateProviderException;
import me.lusory.toothpick.exceptions.IllegalInjectConstructorException;
import me.lusory.toothpick.util.Key;
import me.lusory.toothpick.util.Tuple3;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ToothpickImpl implements Toothpick {
    private final Map<Key<?>, Provider<?>> providers = new ConcurrentHashMap<>();
    private final Map<Key<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Tuple3<Field, Boolean, Key<?>>>> injectFields = new ConcurrentHashMap<>();

    protected ToothpickImpl(Collection<Object> modules) {
        providers.put(Key.of(Toothpick.class), () -> ToothpickImpl.this);

        for (Object module : modules) {
            if (module instanceof Class<?>) {
                try {
                    module = ((Class<?>) module).getConstructor().newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
                    throw new RuntimeException("Module class " + ((Class<?>) module).getName() + " could not be instantiated");
                }
            }

            for (final Method providerMethod : resolveProviders(module.getClass())) {
                addProviderMethod(module, providerMethod);
            }
        }
    }

    @Override
    public <T> T instance(Class<T> type) {
        return createProvider(Key.of(type), null).get();
    }

    @Override
    public <T> T instance(Key<T> key) {
        return createProvider(key, null).get();
    }

    @Override
    public <T> Provider<T> provider(Class<T> type) {
        return createProvider(Key.of(type), null);
    }

    @Override
    public <T> Provider<T> provider(Key<T> key) {
        return createProvider(key, null);
    }

    @Override
    public void injectFields(Object target) {
        if (!injectFields.containsKey(target.getClass())) {
            injectFields.put(target.getClass(), injectFields0(target.getClass()));
        }

        for (final Tuple3<Field, Boolean, Key<?>> injectField : injectFields.get(target.getClass())) {
            final Field field = injectField.getA();
            final Key<?> key = injectField.getC();

            try {
                field.set(target, injectField.getB() ? provider(key) : instance(key));
            } catch (Exception e) {
                throw new RuntimeException("Can't inject field " + field.getName() + " in " + target.getClass().getName());
            }
        }
    }

    private List<Tuple3<Field, Boolean, Key<?>>> injectFields0(Class<?> target) {
        final Set<Field> fields = resolveFields(target);
        final List<Tuple3<Field, Boolean, Key<?>>> result = new ArrayList<>();

        for (Field field : fields) {
            final Class<?> providerType = field.getType().equals(Provider.class)
                    ? (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]
                    : null;

            result.add(
                    Tuple3.of(
                            field,
                            providerType != null,
                            Key.of((Class<?>) (providerType != null ? providerType : field.getType()), createQualifier(field.getAnnotations()))
                    )
            );
        }
        return result;
    }

    private Set<Field> resolveFields(Class<?> type) {
        final Set<Field> fields = new HashSet<>();

        Class<?> current = type;
        while (!current.equals(Object.class)) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }

        return fields;
    }

    private Set<Method> resolveProviders(Class<?> type) {
        final Set<Method> providers = new HashSet<>();

        Class<?> current = type;
        while (!current.equals(Object.class)) {
            for (final Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Provides.class) && (type.equals(current) || isNotProvidedInSubclass(method, providers))) {
                    method.setAccessible(true);
                    providers.add(method);
                }
            }
            current = current.getSuperclass();
        }

        return providers;
    }

    private boolean isNotProvidedInSubclass(Method method, Set<Method> providers) {
        return providers.stream().noneMatch(e -> e.getName().equals(method.getName()) && Arrays.equals(method.getParameterTypes(), e.getParameterTypes()));
    }

    private void addProviderMethod(Object module, Method method) {
        final Key<?> key = Key.of(method.getReturnType(), createQualifier(method.getAnnotations()));
        if (providers.containsKey(key)) {
            throw new DuplicateProviderException(key + " has multiple providers, module " + module.getClass().getName());
        }

        final Singleton singleton = method.getAnnotation(Singleton.class) != null
                ? method.getAnnotation(Singleton.class)
                : method.getReturnType().getAnnotation(Singleton.class);
        final List<Provider<?>> paramProviders = resolveProvidersFromParams(
                key,
                method.getParameterTypes(),
                method.getGenericParameterTypes(),
                method.getParameterAnnotations(),
                Collections.singleton(key)
        );
        providers.put(
                key,
                createSingletonProvider(key, singleton, () -> {
                    try {
                        return method.invoke(module, processParamProviders(paramProviders));
                    } catch (Exception e) {
                        throw new RuntimeException("Can't instantiate " + key + " with provider", e);
                    }
                })
        );
    }

    private List<Provider<?>> resolveProvidersFromParams(
            Key<?> key,
            Class<?>[] parameterClasses,
            Type[] parameterTypes,
            Annotation[][] annotations,
            Set<Key<?>> chain
    ) {
        final List<Provider<?>> providers = new ArrayList<>();

        for (int i = 0; i < parameterTypes.length; ++i) {
            final Class<?> parameterClass = parameterClasses[i];
            final Annotation qualifier = createQualifier(annotations[i]);
            final Class<?> providerType = parameterClass == Provider.class
                    ? (Class<?>) ((ParameterizedType) parameterTypes[i]).getActualTypeArguments()[0]
                    : null;

            System.out.println(chain);
            if (providerType == null) {
                final Key<?> newKey = Key.of(parameterClass, qualifier);
                final Set<Key<?>> newChain = append(chain, key);
                if (newChain.contains(newKey)) {
                    throw new CircularDependencyException(stringifyChain(newChain, newKey));
                }
                providers.add(() -> createProvider(newKey, newChain).get());
            } else {
                providers.add(() -> createProvider(Key.of(providerType, qualifier), null));
            }
        }
        return providers;
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> createSingletonProvider(Key<?> key, Singleton singleton, Provider<T> provider) {
        return singleton != null
                ? () -> {
                    if (!singletons.containsKey(key)) {
                        synchronized (singletons) {
                            if (!singletons.containsKey(key)) {
                                singletons.put(key, provider.get());
                            }
                        }
                    }
                    return (T) singletons.get(key);
                }
                : provider;
    }

    @SuppressWarnings("unchecked")
    private <T> Provider<T> createProvider(Key<T> key, Set<Key<?>> chain) {
        if (!providers.containsKey(key)) {
            final Constructor<?> constructor = resolveConstructor(key);
            final List<Provider<?>> paramProviders = resolveProvidersFromParams(
                    key,
                    constructor.getParameterTypes(),
                    constructor.getGenericParameterTypes(),
                    constructor.getParameterAnnotations(),
                    chain
            );
            providers.put(
                    key,
                    createSingletonProvider(key, key.getType().getAnnotation(Singleton.class), () -> {
                        try {
                            return constructor.newInstance(processParamProviders(paramProviders));
                        } catch (Exception e) {
                            throw new RuntimeException("Can't instantiate " + key + " with provider", e);
                        }
                    })
            );
        }
        return (Provider<T>) providers.get(key);
    }

    private Annotation createQualifier(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(e -> e.annotationType().isAnnotationPresent(Qualifier.class))
                .findFirst()
                .orElse(null);
    }

    private Set<Key<?>> append(Set<Key<?>> set, Key<?> newKey) {
        if (set != null && !set.isEmpty()) {
            return new LinkedHashSet<Key<?>>(set) {
                {
                    add(newKey);
                }
            };
        }
        return Collections.singleton(newKey);
    }

    private String stringifyChain(Set<Key<?>> chain, Key<?> lastKey) {
        final StringBuilder chainString = new StringBuilder();
        for (final Key<?> key : chain) {
            chainString.append(key.toString()).append(" -> ");
        }
        return chainString.append(lastKey.toString()).toString();
    }

    private Object[] processParamProviders(Collection<Provider<?>> paramProviders) {
        return paramProviders.stream()
                .map(Provider::get)
                .toArray();
    }

    private Constructor<?> resolveConstructor(Key<?> key) {
        Constructor<?> inject = null;
        Constructor<?> noarg = null;
        for (final Constructor<?> c : key.getType().getDeclaredConstructors()) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (inject == null) {
                    inject = c;
                } else {
                    throw new IllegalInjectConstructorException(key.getType().getName() + " has multiple @Inject constructors");
                }
            } else if (c.getParameterTypes().length == 0) {
                noarg = c;
            }
        }
        final Constructor<?> ctor = inject != null ? inject : noarg;
        if (ctor != null) {
            ctor.setAccessible(true);
            return ctor;
        }
        throw new IllegalInjectConstructorException(key.getType().getName() + " doesn't have an @Inject or a no-arg constructor, or a module provider");
    }
}
