
package com.didrikfleischer.app.core.di;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.lang.annotation.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Collectors;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BeanDiscoverer {
    private ArrayList<Class> beans;
    // private URLClassLoader classLoader;
    private ClassLoader classLoader;
    public List acceptedAnnotationTypes;
    private String packagePath;

    public BeanDiscoverer(Class[] acceptedAnnotationTypes) throws MalformedURLException { // , String classLoaderUrl
        this.beans = new ArrayList<Class>();
        /* 
        // Loading a classes with another the "default" class loader for the program causes the runtime to 
        // treat the classes as distinct entities and thus type casting is not possible.
        URL classUrl = new URL(classLoaderUrl);
        URL[] classUrls = { classUrl };
        this.classLoader = new URLClassLoader(classUrls);
        System.out.println("Class loader urls");
        Arrays.asList(this.classLoader.getURLs()).stream().forEach(url->System.out.println(url.toExternalForm()));
        */
        this.classLoader = this.getClass().getClassLoader();
        this.acceptedAnnotationTypes = new ArrayList();
        for (Class annotationType : acceptedAnnotationTypes) {
            this.acceptedAnnotationTypes.add(annotationType.getName());
            System.out.println("Accepted annotation type added: " + annotationType.getName());
        }
    }

    public void scan(String[] packagePaths) throws ClassNotFoundException, NullPointerException {        
        for (int i = 0; i < packagePaths.length; i++) {
            System.out.println("About to scann: " + packagePaths[i]);
            System.out.println("Working Directory = " + System.getProperty("user.dir")); // /Users/didrik.fleischer/code/java-server
            this.packagePath = packagePaths[i].replace('.', '/');
            File file = new File(this.packagePath);
            recursiveSearch(file);
        }
    }

    public Class[] getBeans() {
        return (Class[]) this.beans.toArray(new Class[0]);
    }

    public Class[] getSubClasses(Class parentClass) {
        ArrayList<Class> subClasses = new ArrayList<Class>();
        this.beans.stream().forEach((Class cls)-> {
            if (parentClass.isAssignableFrom(cls)) {
                subClasses.add(cls);
            }
        });
        return (Class[]) subClasses.toArray(new Class[0]);
    }

    public Class[] getClassesAnnotatedWith(Class annotationType) {
        ArrayList<Class> annotatedClasses = new ArrayList<Class>();
        this.beans.stream().forEach((Class cls)-> {
            List<Annotation> annotations = Arrays.asList(cls.getAnnotations()); // .stream().map(a->a.annotationType().getName());
            System.out.println("getClassesAnnotatedWith: " + cls.getName());
            // need other way to find matches on annotations. 
            Boolean addAnnotatatedClass = false;
            for(Annotation a : annotations) {
                if (a.annotationType().getName() == annotationType.getName()) {
                    addAnnotatatedClass = true;
                }
            }   

            if (addAnnotatatedClass) {
                annotatedClasses.add(cls);
            }
        });
        return (Class[]) annotatedClasses.toArray(new Class[0]);
    }   

    private void recursiveSearch(File fileOrDirecory) {
        if (fileOrDirecory.isDirectory()) {
            File [] files = fileOrDirecory.listFiles();
            for (int i = 0; i < files.length; i++) {
                recursiveSearch(files[i]);
            }
        } else if (fileOrDirecory.isFile()) {
            try {
                Object obj = resolveClass(fileOrDirecory); 
                if (obj != null) {
                    this.filterAndAdd((Class) obj);
                }
            } catch(ClassNotFoundException ex) {
                System.out.println("FAILED TO RESOLVE SOME CLASS");
                ex.printStackTrace();
            }
        } else {
            System.out.println("ReccursiveSearch on file or directory failed, not a direcotry or a file.");
        }
    }

    private Object resolveClass(File file) throws ClassNotFoundException {
        String re = "\\/(" + this.packagePath +  "\\/.+).class";
        // System.out.println(re);
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(file.getAbsolutePath());
        // System.out.println(file.getAbsolutePath());
        
        if (m.find()) {
            String extractedClassName = m.group(1);
            String binaryClassName = extractedClassName.replace('/', '.');
            binaryClassName = binaryClassName.replace("target.classes.", ""); // Ugly, but I just want it to work.
            Class loadedClass = this.classLoader.loadClass(binaryClassName);
            System.out.println("Loaded CLASS NAME: " + loadedClass.getName());
            return loadedClass;
        } else {
            return null;
        }
    }

    private void filterAndAdd(Class cls) {
        Annotation[] annotations = cls.getAnnotations();
        if (annotations.length > 0){
            Boolean addClass = false;
            for (int i = 0; i < annotations.length; i++) {
                if (this.acceptedAnnotationTypes.contains(annotations[i].annotationType().getName())) {
                    addClass = true;
                } else {
                    System.out.println("Annotation type " + annotations[i].annotationType().getName() + " is not accepted");
                }
            }
            if (addClass) {
                System.out.println("BeanDiscoverer# Adding class with name: " + cls.getName());
                this.beans.add(cls);
            }
        }
    }

    public String getAsString() {
        Iterator<Class> b = this.beans.iterator();
        String res = "BeanDiscoverer Content: \n";
        while(b.hasNext()) {
            Class c = b.next();
            res += "Bean: " + c.getName() + "\n";
        }
        return res;
    }
}

// The DI container can use the store to get all the annotated types and set it self up.

// Other parts of the system can also use the BeanDiscoverer's store to build the router, middleware stack and
// alter the behavior of methods (the method return value f.eks) to parse output as json and use as HTTP response body.


/*
the package path allways starts at the top level and is relative to the class path. 
This means that the class path + package path + file name = full path of class
*/



/*

private static final String CLASS_FOLDER =
        "/Users/juneyoungoh/Downloads/";

private static Class getClassFromFile(String fullClassName) throws Exception {
    URLClassLoader loader = new URLClassLoader(new URL[] {
            new URL("file://" + CLASS_FOLDER)
    });
    return loader.loadClass(fullClassName);
}

public static void main( String[] args ) throws Exception {
    System.out.println((getClassFromFile("ClassFile"));
}

*/

