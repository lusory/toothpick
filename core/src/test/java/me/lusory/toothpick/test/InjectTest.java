package me.lusory.toothpick.test;

import me.lusory.toothpick.Toothpick;
import me.lusory.toothpick.exceptions.CircularDependencyException;
import me.lusory.toothpick.test.mock.ExampleClass1;
import me.lusory.toothpick.test.mock.ExampleClass2;
import me.lusory.toothpick.test.mock.ExampleClass3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InjectTest {
    @Test
    void inject() {
        final Toothpick injector = Toothpick.of(ExampleClass1.class, ExampleClass2.class);

        Assertions.assertEquals(injector.instance(String.class), "Hello World! 0");
        Assertions.assertEquals(injector.instance(int.class), 0);
    }

    @Test // TODO: fix stackoverflowerror between two providers
    void circularDependency() {
        Assertions.assertThrows(CircularDependencyException.class, () -> Toothpick.of(ExampleClass3.class).instance(ExampleClass3.class));
    }
}
