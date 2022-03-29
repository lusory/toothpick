package me.lusory.toothpick;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class ProviderImpl<T> implements Provider<T> {
    @Nullable
    private final String name;
    private final Type type;
    @Nullable
    private final Class<? extends Annotation> qualifier;
    private final Supplier<T> supplier;
    private final boolean isSingleton;
    @EqualsAndHashCode.Exclude
    private T value = null;

    @Override
    public T get() {
        return isSingleton ? (value != null ? value : (value = supplier.get())) : supplier.get();
    }
}
