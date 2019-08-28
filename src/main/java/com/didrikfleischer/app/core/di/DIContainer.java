package com.didrikfleischer.app.core.di;

import com.didrikfleischer.app.core.di.Resolver;
import com.didrikfleischer.app.core.di.annotations.Configuration;
import com.didrikfleischer.app.core.di.annotations.Component;


import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


/**
 * Spec:
 * - Only one constructor per class
 * - Everything is scoped globally
 * - Interfaces and classes have only one "resolver" which is either of type class or a method
 * - "resolver" methods are configured via methods annotated with @Bean
 * - "resolver" methods are alone responsible for generating the associated class
 * - The DI container can build arbitrary deep dependency trees (recursion and reflection needs to be used)
 */

public class DIContainer {
    private Map<String, Resolver> resolvers;


    public DIContainer() {
        this.resolvers = new HashMap<String, Resolver>();
    }

    public void configure(Class[] confClasses) {
        for(int i = 0; i < confClasses.length; i++) {
            Class cls = confClasses[i];
            System.out.println("Configuring class in container: " + cls.getName());
            Annotation[] annotations = cls.getAnnotations();
            for(int j = 0; j < annotations.length; j++) {
                System.out.println("Annotation: " + annotations[j].annotationType().getName());

                String annotationName = annotations[j].annotationType().getName();

                if (annotationName.equals("com.didrikfleischer.app.core.di.annotations.Configuration")) {
                    // loop over the methods annotated with @Bean
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        // System.out.println("Adding method based resolver with method name: " + method.getName());
                        String methodName = method.getName();
                        String methodReturnTypeName = method.getReturnType().getName();

                        Resolver resolver = new Resolver();
                        resolver.setMethodResolver(cls, methodName); // likely will not work, in which case I need to pass the class and the method name instead probably.
                        this.setResolver(methodReturnTypeName, resolver);
                    }
                } else if (annotationName.equals("com.didrikfleischer.app.core.di.annotations.Component")) {
                    // This is a class to class binding
                    Resolver resolver = new Resolver();
                    resolver.setClassResolver(cls); // likely will not work, in which case I need to pass the class and the method name instead probably.
                    this.setResolver(cls.getName(), resolver);
                }
            }
        }
    }

    public void bindClass(Class cls) {
        Resolver resolver = new Resolver();
        resolver.setClassResolver(cls); // likely will not work, in which case I need to pass the class and the method name instead probably.
        this.setResolver(cls.getName(), resolver);
    }

    public void bindInstance(Object instance) {
        Resolver resolver = new Resolver();
        resolver.setInstanceResolver(instance);
        this.setResolver(instance.getClass().getName(), resolver);
    }

    public Object getBean(Class classOrInterface, Object... args) {
        try {
            String classOrInterfaceName = classOrInterface.getName();
            System.out.println("About to try to resolve: " + classOrInterfaceName);
            Resolver startResolver = this.resolvers.get(classOrInterfaceName); // How to work with classes and interfaces at the same time?
            System.out.println("Start Resolver: " + startResolver.getAsString());
            Object instance = this.recursivelyBuildInstance(startResolver);
            return instance;

        } catch (Exception ex) {
            System.out.println("FAILED TO BUILD REQUESTED CLASS OR INTERFACE!");
            ex.printStackTrace();
            return null;
        }
    }

    public Object[] getMultipleBeans(Class[] classesOrInterfaces) {
        List beans = new ArrayList();
        for (Class cls : classesOrInterfaces) {
            Object instance = this.getBean(cls);
            beans.add(instance);
        }
        return beans.toArray();
    }


    public String getAsString() {
        String result = "";
        result += "Container Resolvers: \n";
        for (Map.Entry<String, Resolver> entry : this.resolvers.entrySet()) {
            result += " - " + entry.getKey() + ": " + entry.getValue().getAsString() + "\n";
        }
        return result;        
    }


    private Object recursivelyBuildInstance(Resolver resolver) throws Exception {
        try {
            if (resolver.type == "method") {
                Method resolverMethod = resolver.configClass.getMethod(resolver.method, new Class[0]); // a limitation is that the resolver methods cannot have arguments.         
                Object configInstance = resolver.configClass.getDeclaredConstructor().newInstance();
                return resolverMethod.invoke(configInstance);
            } else if (resolver.type == "class") {    
                // can later introduce default parameters and resolver arguments to override the defaults.
                Object[] constructorParameters = new Object[resolver.constructorParameterTypes.length]; // instances
                for (int i = 0; i < resolver.constructorParameterTypes.length; i++) {
                    String paramTypeName = resolver.constructorParameterTypes[i].getName(); // Do I not cast interfaces to classes here?
                    Resolver paramResolver = this.resolvers.get(paramTypeName);
                    // System.out.println("Calling recursivelyBuildInstance on: " + paramType.getName());
                    Object paramInstance = recursivelyBuildInstance(paramResolver);
                    constructorParameters[i] = paramInstance;
                    // System.out.println("Name of class of resolved parameter: " + paramInstance.getClass().getName());
                }
                
                // constructorParameters could be merged with provided arguments in the future. See Resolver.setClassResolver()
                return resolver.cls.getDeclaredConstructor(resolver.constructorParameterTypes).newInstance(constructorParameters); 
            } else if (resolver.type == "instance") {
                return resolver.instance;
            } else {
                throw new Exception("Unknown resolver type!");
            }
        } catch (Exception ex) {
            System.out.println("Failed to recursively build instance");
            ex.printStackTrace();
            throw new Exception("Failed to build instance from resolver."); 
        }

    }

    private void setResolver(String typeName, Resolver resolver) {
        this.resolvers.put(typeName, resolver);
    }
}
