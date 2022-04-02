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

import me.lusory.toothpick.impl.InjectorImpl;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

public interface Injector {
    static Injector of(Object... modules) {
        return of(Arrays.asList(modules));
    }

    static Injector of(Collection<Object> modules) {
        return new InjectorImpl(modules);
    }

    default <T> T instance(Type type) {
        return instance(type, null, null);
    }

    default <T> T instance(Type type, @Nullable String name) {
        return instance(type, name, Named.class);
    }

    default <T> T instance(Type type, @Nullable Class<? extends Annotation> qualifier) {
        return instance(type, null, qualifier);
    }

    @SuppressWarnings("unchecked")
    default <T> T instance(Type type, @Nullable String name, @Nullable Class<? extends Annotation> qualifier) {
        return (T) provider(type, name, qualifier).get();
    }

    default <T> Provider<T> provider(Type type) {
        return provider(type, null, null);
    }

    default <T> Provider<T> provider(Type type, @Nullable String name) {
        return provider(type, name, Named.class);
    }

    default <T> Provider<T> provider(Type type, @Nullable Class<? extends Annotation> qualifier) {
        return provider(type, null, qualifier);
    }

    <T> Provider<T> provider(Type type, @Nullable String name, @Nullable Class<? extends Annotation> qualifier);
}
