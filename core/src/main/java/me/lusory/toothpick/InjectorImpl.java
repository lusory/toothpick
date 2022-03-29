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

            if (module instanceof Class<?>) {
                final Annotation qualifier = getQualifier(moduleClass.getAnnotations());
                final Class<? extends Annotation> qualifierType = qualifier != null ? qualifier.annotationType() : null;
                final String qualifierName = qualifierType == Named.class ? ((Named) qualifier).value() : null;

                try {
                    final Constructor<?> moduleCtor = moduleClass.getConstructor();

                    // add module
                    providers.add(
                            new ProviderImpl<>(
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
                            )
                    );
                } catch (NoSuchMethodException ignored) {
                    boolean hasFoundCtor = false;
                    for (final Constructor<?> ctor : Reflect.getConstructors(moduleClass)) {
                        if (ctor.isAnnotationPresent(Inject.class)) {
                            ctor.setAccessible(true);

                            if (getProvider(p -> Reflect.isTypeEqual(p.getType(), moduleClass)) != null) {
                                throw new DuplicateProviderException(moduleClass.getName());
                            }

                            providers.add(
                                    new ProviderImpl<>(
                                            qualifierName,
                                            moduleClass,
                                            qualifierType,
                                            () -> {
                                                Provider<?>[] argProviders = new Provider[0];

                                                for (final Parameter param : ctor.getParameters()) {
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

                                                try {
                                                    return ctor.newInstance(args);
                                                } catch (Exception e) {
                                                    throw new DependencyResolveException(e);
                                                }
                                            },
                                            true
                                    )
                            );

                            hasFoundCtor = true;
                            break;
                        }
                    }
                    if (!hasFoundCtor) {
                        throw new InjectorException("Could not find @Inject or no-arg constructor for " + ((Class<?>) module).getName());
                    }
                }
            }

            // discover @Provides annotated methods
            for (final Method method : Reflect.getMethods(moduleClass)) {
                populateMethod(() -> provider(moduleClass).get(), method);
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
                                Provider<?>[] argProviders = new Provider[0];

                                for (final Parameter param : method.getParameters()) {
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> Provider<T> provider(Type type, @Nullable String name, @Nullable Class<? extends Annotation> qualifier) {
        final Class<? extends Annotation> qualifier0 = name != null && qualifier == null ? Named.class : qualifier;
        return (Provider<T>) Assertions.nonNullElseThrow(
                getProvider(p -> {
                    if (qualifier0 != null) {
                        return Reflect.isTypeEqual(p.getType(), type)
                                && p.getQualifier() == qualifier0
                                && Objects.equals(p.getName(), name);
                    }
                    return Reflect.isTypeEqual(p.getType(), type);
                }),
                () -> new DependencyResolveException("Provider not found for " + type.getTypeName())
        );
    }
}
