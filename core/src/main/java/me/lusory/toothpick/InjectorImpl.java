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
import me.lusory.toothpick.exceptions.DependencyResolveException;
import me.lusory.toothpick.exceptions.DuplicateProviderException;
import me.lusory.toothpick.exceptions.InjectorException;
import me.lusory.toothpick.util.Assertions;
import me.lusory.toothpick.util.Reflect;
import org.jetbrains.annotations.Nullable;

import javax.inject.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

class InjectorImpl implements Injector {
    private final Set<Provider<?>> providers = new HashSet<>();

    protected InjectorImpl(Collection<Object> modules) {
        populate(modules);
    }

    private void populate(Collection<Object> modules) {
        for (final Object module : modules) {
            final Class<?> moduleClass = module instanceof Class<?> ? (Class<?>) module : module.getClass();

            final Annotation qualifier = getQualifier(moduleClass.getAnnotations());
            final Class<? extends Annotation> qualifierType = qualifier != null ? qualifier.annotationType() : null;
            final String qualifierName = qualifierType == Named.class ? ((Named) qualifier).value() : null;

            final Provider<?> moduleProvider = module instanceof Class<?>
                    ? makeProvider(moduleClass, qualifierName, qualifierType)
                    : new ProviderImpl<>(qualifierName, moduleClass, qualifierType, () -> module, true);

            providers.add(moduleProvider);

            // discover @Provides annotated methods
            for (final Method method : Reflect.getMethods(moduleClass)) {
                populateMethod(moduleProvider::get, method);
            }
        }
    }

    private void populateMethod(Supplier<Object> module, Method method) {
        if (method.isAnnotationPresent(Provides.class)) {
            method.setAccessible(true);

            final Type returnType = method.getGenericReturnType();
            if (getProvider(p -> Reflect.isTypeEqual(p.getType(), returnType)) != null) {
                throw new DuplicateProviderException(returnType.getTypeName());
            }

            final Annotation qualifier = Assertions.nonNullElse(
                    getQualifier(method.getAnnotations()),
                    () -> getQualifier(method.getReturnType().getAnnotations())
            );
            final Class<? extends Annotation> qualifierType = qualifier != null ? qualifier.annotationType() : null;
            final String qualifierName = qualifierType == Named.class ? ((Named) qualifier).value() : null;

            providers.add(
                    new ProviderImpl<>(
                            qualifierName,
                            returnType,
                            qualifierType,
                            () -> {
                                final Object[] args = resolveParams(method);

                                try {
                                    return method.invoke(module.get(), args);
                                } catch (Exception e) {
                                    throw new DependencyResolveException(e);
                                }
                            },
                            method.isAnnotationPresent(Singleton.class) || method.getReturnType().isAnnotationPresent(Singleton.class)
                    )
            );
        }
    }

    private Provider<?> makeProvider(Class<?> moduleClass, @Nullable String qualifierName, @Nullable Class<? extends Annotation> qualifierType) {
        Provider<?> moduleProvider;

        try {
            final Constructor<?> moduleCtor = moduleClass.getConstructor();

            moduleCtor.setAccessible(true);

            moduleProvider = new ProviderImpl<>(
                    qualifierName,
                    moduleClass,
                    qualifierType,
                    () -> {
                        try {
                            return moduleCtor.newInstance();
                        } catch (Exception e) {
                            throw new InjectorException("Could not instantiate module " + moduleClass.getName(), e);
                        }
                    },
                    true
            );
        } catch (NoSuchMethodException ignored) {
            final Constructor<?> moduleCtor = Assertions.nonNullElseThrow(
                    getInjectConstructor(moduleClass),
                    () -> new InjectorException("Could not find @Inject or no-arg constructor for " + moduleClass.getName())
            );

            moduleCtor.setAccessible(true);

            moduleProvider = new ProviderImpl<>(
                    qualifierName,
                    moduleClass,
                    qualifierType,
                    () -> {
                        final Object[] args = resolveParams(moduleCtor);

                        try {
                            return moduleCtor.newInstance(args);
                        } catch (Exception e) {
                            throw new DependencyResolveException(e);
                        }
                    },
                    true
            );
        }

        return moduleProvider;
    }

    private Annotation getQualifier(Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }

    private @Nullable Provider<?> getProvider(Predicate<ProviderImpl<?>> predicate) {
        for (final Provider<?> provider : providers) {
            if (predicate.test((ProviderImpl<?>) provider)) {
                return provider;
            }
        }
        return null;
    }

    private @Nullable Constructor<?> getInjectConstructor(Class<?> type) {
        for (final Constructor<?> ctor : Reflect.getConstructors(type)) {
            if (ctor.isAnnotationPresent(Inject.class)) {
                return ctor;
            }
        }
        return null;
    }

    private Object[] resolveParams(Executable exc) {
        Provider<?>[] argProviders = new Provider[0];

        for (final Parameter param : exc.getParameters()) {
            final Annotation qualifier0 = Assertions.nonNullElse(
                    getQualifier(param.getAnnotations()),
                    () -> getQualifier(param.getType().getAnnotations())
            );
            final Class<? extends Annotation> qualifierType0 = qualifier0 != null ? qualifier0.annotationType() : null;
            final String qualifierName0 = qualifierType0 == Named.class ? ((Named) qualifier0).value() : null;

            final Provider<?> provider = provider(
                    param.getParameterizedType(),
                    qualifierName0,
                    qualifierType0
            );

            argProviders = Arrays.copyOf(argProviders, argProviders.length + 1);
            argProviders[argProviders.length - 1] = provider;
        }

        final Object[] args = new Object[argProviders.length];
        for (int i = 0; i < argProviders.length; i++) {
            args[i] = argProviders[i].get();
        }

        return args;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Provider<T> provider(Type type, @Nullable String name, @Nullable Class<? extends Annotation> qualifier) {
        final Class<? extends Annotation> qualifier0 = name != null && qualifier == null ? Named.class : qualifier;
        return (Provider<T>) Assertions.nonNullElse(
                getProvider(p -> {
                    if (qualifier0 != null) {
                        return Reflect.isTypeEqual(p.getType(), type)
                                && p.getQualifier() == qualifier0
                                && Objects.equals(p.getName(), name);
                    }
                    return Reflect.isTypeEqual(p.getType(), type);
                }),
                () -> {
                    final Class<?> moduleClass = Reflect.typeToClass(type);
                    final Provider<?> provider = makeProvider(moduleClass, name, qualifier0);

                    providers.add(provider);

                    // discover @Provides annotated methods
                    for (final Method method : Reflect.getMethods(moduleClass)) {
                        populateMethod(provider::get, method);
                    }

                    return provider;
                }
        );
    }
}
