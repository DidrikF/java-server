package com.didrikfleischer.app.core.di.annotations;

import java.lang.annotation.*;  

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) // I want the annotation to apply to classes
public @interface ResponseBody{}

