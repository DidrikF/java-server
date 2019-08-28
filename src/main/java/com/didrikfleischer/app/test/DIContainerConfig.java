package com.didrikfleischer.app.test;

import com.didrikfleischer.app.core.di.annotations.Component;
import com.didrikfleischer.app.core.di.annotations.Bean;
import com.didrikfleischer.app.core.di.annotations.Configuration;

import com.didrikfleischer.app.test.beans.*;

/**
 * This is used to configure the DI container and tell it how to build objects.
 * This is mainly intended to bind interfaces to implementations, but can also be used to declare 
 * explicitly how to create an object.
 */
@Configuration
public class DIContainerConfig {
    
    @Bean
    public Animal getAnimal() { // resolve an interface (only way to bind interface to implementation)
        Mother mother = new Mother();
        Owner owner = new Owner(mother);
        Owner owner2 = new Owner(mother);

        return new Fido(owner, owner2);
    }

    @Bean
    public Dog getDog() { // resolve a class with a method
        return (Dog) this.getAnimal();
    }

    // maybe return a class to hand over instantiation to the DI container when binding an interface to a class.

}