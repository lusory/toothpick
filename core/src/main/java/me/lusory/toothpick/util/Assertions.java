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
