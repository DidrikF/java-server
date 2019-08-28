package com.didrikfleischer.app.core.di;

import java.util.*;

import java.lang.reflect.Constructor;

public class Resolver {
    String type;
    
    Class configClass;
    String method;

    Class cls;

    Object instance;

    // Object[] constructorParameters;
    // Making the reolver take parameter types and allready instantiated parameters would be a major upgrade to the system... 
    Class[] constructorParameterTypes;

    // I can create a simple singleton by caching the instance on the resolver and return that same object in the future

    public void setMethodResolver(Class configClass, String method) { // method resolvers does not accept arguments atm 
        this.type = "method";
        this.method = method;
        this.configClass = configClass;
    }

    public void setClassResolver(Class cls, Object... args) { 
        // the args are not used atm, they might be used in the future to pass arguments for object instantiation...
        // Currently all objects to be resolved must be configured in the DIContainer!
        this.type = "class";
        this.cls = cls;
        // this.constructorParameters = args; // Not used!

        Constructor clsConstructor = this.cls.getDeclaredConstructors()[0]; // Ok since I only allow one constructor.

        Class[] constructorParameterTypes = clsConstructor.getParameterTypes(); // Assuming all need to be instantiated... 

        this.constructorParameterTypes = constructorParameterTypes;
        for (Class type : constructorParameterTypes) {
            System.out.println("resolver.constructorParameterType: " + type.getName());
        }
    }

    public void setInstanceResolver(Object instance) {
        this.type = "instance";
        this.instance = instance;
    }

    public String getAsString() {
        if (this.type == "method") {
            return "{type: method, method name: " + this.method + "}";
        } else if (this.type == "class") {
            return "{type: class, class name: " + this.cls.getName() + "}";            
        } else if (this.type == "instance") {
            return "{type: instance, instance class name " + this.instance.getClass().getName() + "}";
        }
        return "Resolver of unknown type";
    }
}