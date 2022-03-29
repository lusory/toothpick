package me.lusory.toothpick.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Supplier;

@UtilityClass
public class Assertions {
    public @NotNull <T> T nonNullElseThrow(@Nullable T obj, Supplier<? extends RuntimeException> excSupplier) {
        if (obj == null) {
            throw excSupplier.get();
        }
        return obj;
    }

    public @UnknownNullability <T> T nonNullElse(@Nullable T obj, Supplier<T> elseSupplier) {
        if (obj == null) {
            return elseSupplier.get();
        }
        return obj;
    }
}
