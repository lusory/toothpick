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

import me.lusory.toothpick.util.Key;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collection;

public interface Injector {
    static Injector of(Object... modules) {
        return of(Arrays.asList(modules));
    }

    static Injector of(Collection<Object> modules) {
        return new InjectorImpl(modules);
    }

    <T> T instance(Class<T> type);

    <T> T instance(Key<T> key);

    <T> Provider<T> provider(Class<T> type);

    <T> Provider<T> provider(Key<T> key);
}
