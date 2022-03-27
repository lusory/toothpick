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
