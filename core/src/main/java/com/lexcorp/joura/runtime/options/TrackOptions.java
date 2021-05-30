package com.lexcorp.joura.runtime.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TrackOptions {

    boolean always() default true;

    Strategy strategy() default Strategy.ALWAYS_TRACK;

}
