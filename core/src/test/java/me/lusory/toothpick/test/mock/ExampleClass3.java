package me.lusory.toothpick.test.mock;

import me.lusory.toothpick.annotations.Provides;

import javax.inject.Singleton;

public class ExampleClass3 {
    @Provides
    @Singleton
    public ExampleClass2 exampleClass2() {
        return new ExampleClass2();
    }
}
