package me.lusory.toothpick.test.mock;

import me.lusory.toothpick.annotations.Autowired;

public class ExampleClass1 {
    @Autowired
    public ExampleClass1(ExampleClass2 exampleClass2) {
        System.out.println(exampleClass2.toString());
    }
}
