package com.didrikfleischer.app.core.di.annotations;

import java.lang.annotation.*;  

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) 
public @interface Bean{
    String name() default "NO_NAME";
};

