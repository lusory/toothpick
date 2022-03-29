package me.lusory.toothpick.test;

import me.lusory.toothpick.Injector;
import me.lusory.toothpick.test.mock.ExampleClass1;
import me.lusory.toothpick.test.mock.ExampleClass2;
import me.lusory.toothpick.test.mock.ExampleClass3;
import org.junit.jupiter.api.Test;

public class InjectorTest {
    @Test
    public void inject() {
        final Injector injector = Injector.of(ExampleClass3.class, ExampleClass2.class, ExampleClass1.class);
        System.out.println(
                injector.instance(ExampleClass1.class).getClass().getName()
        );
    }
}
