package me.lusory.toothpick.test.mock;

import me.lusory.toothpick.annotations.Provides;

import javax.inject.Singleton;

public class ExampleClass3 {
    @Provides
    @Singleton
    public ExampleClass3 provideExampleClass4(ExampleClass3 exampleClass3) {
        return new ExampleClass3();
    }
}
