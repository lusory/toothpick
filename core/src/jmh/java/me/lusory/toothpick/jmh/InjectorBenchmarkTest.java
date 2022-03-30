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

package me.lusory.toothpick.jmh;

import com.google.inject.Guice;
import me.lusory.toothpick.Injector;
import me.lusory.toothpick.jmh.mock.ExampleClass1;
import me.lusory.toothpick.jmh.mock.ExampleClass2;
import me.lusory.toothpick.jmh.mock.ExampleClass3;
import org.codejargon.feather.Feather;
import org.openjdk.jmh.annotations.Benchmark;

public class InjectorBenchmarkTest {
    @Benchmark
    public void injectToothpick() {
        final Injector injector = Injector.of(ExampleClass3.class, ExampleClass2.class, ExampleClass1.class);
        injector.instance(ExampleClass1.class);
    }

    @Benchmark
    public void injectFeather() {
        final Feather injector = Feather.with(new ExampleClass3(), new ExampleClass2());
        injector.instance(ExampleClass1.class);
    }

    @Benchmark
    public void injectGuice() {
        final com.google.inject.Injector injector = Guice.createInjector();
        injector.getInstance(ExampleClass1.class);
    }
}
