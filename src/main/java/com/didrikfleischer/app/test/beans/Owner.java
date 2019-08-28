package com.didrikfleischer.app.test.beans;

import com.didrikfleischer.app.core.di.annotations.Component;

@Component
public class Owner {
    public Mother mother;

    public Owner(Mother mother) {
        this.mother = mother;
    }
}