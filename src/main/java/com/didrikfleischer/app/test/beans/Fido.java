package com.didrikfleischer.app.test.beans;

import com.didrikfleischer.app.core.di.annotations.Component;

@Component
public class Fido extends Dog implements Animal {
    public Owner owner;
    public Owner owner2;


    public Fido(Owner owner, Owner owner2) {
        this.owner = owner;
        this.owner2 = owner2;
    }


    public void bark() {
        System.out.println("WOOF!");
    }

    public String getBark() {
        return "WOOF!";
    }

}