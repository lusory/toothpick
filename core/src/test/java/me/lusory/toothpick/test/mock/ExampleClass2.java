package me.lusory.toothpick.test.mock;

import me.lusory.toothpick.annotations.Provides;

import javax.inject.Singleton;

public class ExampleClass2 {
    @Provides
    @Singleton
    public int provideNum() {
        return 0;
    }
}
