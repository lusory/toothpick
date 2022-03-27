package me.lusory.toothpick.test.mock;

import me.lusory.toothpick.annotations.Provides;

import javax.inject.Singleton;

public class ExampleClass1 {
    @Provides
    @Singleton
    public String provideString(int number) {
        return "Hello World! " + number;
    }
}
