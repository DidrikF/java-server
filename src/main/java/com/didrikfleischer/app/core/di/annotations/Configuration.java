package com.didrikfleischer.app.core.di.annotations;

import java.lang.annotation.*;  

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) // I want the annotation to apply to classes
public @interface Configuration{}

