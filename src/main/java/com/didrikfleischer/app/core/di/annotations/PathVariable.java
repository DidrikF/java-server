package com.didrikfleischer.app.core.di.annotations;

import java.lang.annotation.*;  

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER) 
public @interface PathVariable{
    String value();
};

