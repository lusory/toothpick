package me.lusory.toothpick.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class Assertions {
    @SneakyThrows
    public @NotNull Object throwIfNull(@Nullable Object object, Class<? extends Exception> e) {
        if (object == null) {
            throw (Exception) e.getConstructor().newInstance();
        }
        return object;
    }
}
