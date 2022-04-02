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

package me.lusory.toothpick.impl;

import lombok.*;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ProviderImpl<T> implements Provider<T> {
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
