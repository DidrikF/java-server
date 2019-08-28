package com.didrikfleischer.app;
import com.didrikfleischer.app.core.di.DIContainer;
import com.didrikfleischer.app.core.di.BeanDiscoverer;

import com.didrikfleischer.app.core.di.annotations.*;

import com.didrikfleischer.app.test.beans.Fido;
import com.didrikfleischer.app.test.beans.Mammal;
import com.didrikfleischer.app.test.beans.Owner;
import com.didrikfleischer.app.test.beans.Mother;
import com.didrikfleischer.app.test.beans.Animal;
import com.didrikfleischer.app.test.DIContainerConfig;
import java.net.MalformedURLException;

public class DITest {
    public static void main(String[] args) {
        try {
            Class[] acceptedClassAnnotationTypes = {
                Configuration.class,
                Bean.class,
                Component.class,
                Controller.class,
            };

            BeanDiscoverer discoverer = new BeanDiscoverer(acceptedClassAnnotationTypes); // , "file:///Users/didrik.fleischer/code/java-server/target/classes/"
            String[] packages = {"target.classes.com.didrikfleischer.app.test"}; // needs to be relative to the working direcory: xyz/java-server and target.classes is stripped when loading the class from the class loader
            discoverer.scan(packages);  

            System.out.println(discoverer.getAsString());
            Class[] DIContainerConfigurationObjects = discoverer.getBeans(); 

            DIContainer container = new DIContainer();
    
            container.configure(DIContainerConfigurationObjects);
    
            System.out.println(container.getAsString());

            Fido fido = (Fido) container.getBean(Fido.class);
            Fido fido2 = (Fido) container.getBean(Animal.class); // but Animal is an interface!
    
            System.out.println("Fido1: ");        
            fido.bark();
            System.out.println("Fido2: ");
            fido2.bark();
    
            fido.owner.mother.shout();
            fido.owner2.mother.shout();
        } catch(MalformedURLException ex) {
            System.out.println("EXCEPTION FROM URL");
            ex.printStackTrace();
        } catch(ClassNotFoundException ex) {
            System.out.println("EXCEPTION FROM loadClass");
            ex.printStackTrace();
        }
        
    }
}


/*
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.CLASS)  
@interface Inject {
    int value() default 0;  
}

@Inject(value=10)


// Using anotations
Hello h=new Hello();  
Method m=h.getClass().getMethod("sayHello");  
  
MyAnnotation manno=m.getAnnotation(MyAnnotation.class);  
System.out.println("value is: "+manno.value());  




try {
    Class clazz = Class.forName(cls.getName());
    return clazz.getDeclaredConstructor().newInstance();
} catch(ReflectiveOperationException ex) {
    System.out.println("Couldn't find or instantiate an implementation of the given class.");
    return null;
}

*/




