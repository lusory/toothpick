package me.lusory.toothpick.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Conditional {
    Class<?> condition();
    String method();
}
