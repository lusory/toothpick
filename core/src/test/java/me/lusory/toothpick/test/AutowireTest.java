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

package me.lusory.toothpick.test;

import me.lusory.toothpick.Toothpick;
import me.lusory.toothpick.test.mock.ExampleClass1;
import me.lusory.toothpick.test.mock.ExampleClass2;
import org.junit.jupiter.api.Test;

public class AutowireTest {
    @Test
    void autowire() {
        Toothpick.of(ExampleClass1.class, ExampleClass2.class);
    }
}
