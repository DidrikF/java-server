package com.didrikfleischer.app.test.beans;

import com.didrikfleischer.app.core.di.annotations.Component;

@Component
public class Mother {
    public void shout() {
        System.out.println("DINER IS READY!");
    }
}