package me.lusory.toothpick.jmh;

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
}
