package com.lexcorp.joura;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lexcorp.joura.utils.Strategy;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TrackOptions {

    boolean alwaysTrack() default false;

    Strategy analysingStrategy() default Strategy.DEFAULT;

}
