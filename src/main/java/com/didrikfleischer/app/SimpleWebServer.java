package com.didrikfleischer.app;
// build with output directory: javac SimpleWebServer.java -d build

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.net.MalformedURLException;

import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;
import com.didrikfleischer.app.core.di.BeanDiscoverer;
import com.didrikfleischer.app.core.di.DIContainer;
import com.didrikfleischer.app.core.di.annotations.*;
import com.didrikfleischer.app.core.router.Router;
import com.didrikfleischer.app.core.di.annotations.Controller;
import com.didrikfleischer.app.core.router.Filter;
import com.didrikfleischer.app.core.router.Route;
// Need to import them, otherwise they are not part of the build and not accessible for reflection.
import com.didrikfleischer.app.application.controllers.JokeController;
import com.didrikfleischer.app.application.controllers.StaticContentController;
import com.didrikfleischer.app.application.filters.AuthFilter;
import com.didrikfleischer.app.application.filters.TestFilterA;
import com.didrikfleischer.app.application.filters.TestFilterB;

import com.didrikfleischer.app.test.DIContainerConfig;
import com.didrikfleischer.app.test.beans.*;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleWebServer {
    public static DIContainer container; // how is access to this in static methods?
    public static Router router;
    public static BeanDiscoverer discoverer;

    public static void main(String[] argv) {

        bootstrap();

        try (ServerSocket server = new ServerSocket(8080)) {
            // server.setSoTimeout(1000); // the server.accept() call will only block for
            // one second.
            while (true) {
                try (Socket socket = server.accept();) { // Blocks until a connection is made.
                    try (
                        InputStream input = socket.getInputStream(); // will close the streams before exiting the try block
                        OutputStream output = socket.getOutputStream();
                    ) {
                        // This does kind of what a serverlet would do (parse request, match to handler, write response to TCP connection)
                        Request request = new Request(input); 
                        System.out.println("Original Request: ");
                        System.out.println(request.originalRequest);
                        
                        Properties defaultHeaders = new Properties();
                        // defaultHeaders.put("Connection", "close");
                        defaultHeaders.put("Content-Language", "en");
                        defaultHeaders.put("Server", "Epic Java Server");
                        defaultHeaders.put("Access-Control-Allow-Origin", "*");

                        Response response = new Response(output, defaultHeaders);
                        container.bindInstance(request);
                        container.bindInstance(response);

                        // System.out.println("Request: \n" + request.getAsString());

                        response = router.handleRequest();

                        System.out.println("FINAL RESPONSE: " + response.composeResponse());
                        
                        response.send(); // What about files, I might want to use a different send method.
                    } catch(IOException ex) {
                        // Dont know what to do with this... its about cleaning up after writing to the output stream failed in some way.
                        System.out.println("Writing to ouput stream failed in some way.");
                        ex.printStackTrace();
                    } catch(Exception ex) {
                        // Malformed startLine ....
                        System.out.println("#Exception in request/response handling (can be malformed start line or something else)");
                        ex.printStackTrace();
                    }
                } catch (SocketTimeoutException ex) {
                    System.out.println("The server.accept() call timed out!"); // Why is is usefull to time out?
                    if (Thread.currentThread().isInterrupted()) { // What is the purpose of this?
                        break; // break out of infinite loop, effectivly exiting the program.
                    }
                } 
            }
        } catch (Exception e) {
            System.out.println("Uncaughet exceptions: ");
            System.out.println(e);
            e.printStackTrace();

            // exceptions thrown by statesments in the try-resouce-statements
            Throwable[] supressedExceptions = e.getSuppressed();
            System.out.println("Suppressed Exceptions: ");
            System.out.println(supressedExceptions);

        }
    }


    private static void bootstrap() {
        // Search for beans
        Class[] acceptedClassAnnotationTypes = {
            Bean.class,
            Component.class,
            Configuration.class,
            Controller.class
        };
        try {
            discoverer = new BeanDiscoverer(acceptedClassAnnotationTypes); // , "file:///Users/didrik.fleischer/code/java-server/target/classes/com/didrikfleischer/app"
            String[] packagePaths = {
                "target.classes.com.didrikfleischer.app.application.controllers", 
                "target.classes.com.didrikfleischer.app.application.filters", 
                "target.classes.com.didrikfleischer.app.test"
            }; // com.didrikfleischer.app.
            discoverer.scan(packagePaths);  
            System.out.println(discoverer.getAsString());
        } catch(MalformedURLException ex) {
            System.out.println("EXCEPTION FROM URL");
            ex.printStackTrace();
        } catch(ClassNotFoundException ex) {
            System.out.println("EXCEPTION FROM loadClass");
            ex.printStackTrace();
        } catch(NullPointerException ex) {
            System.out.println("EXCEPTION FROM scann");
            ex.printStackTrace();
        }
        Class[] DIContainerConfigurationObjects = discoverer.getBeans();
        
        // Build DI container
        container = new DIContainer();
        container.configure(DIContainerConfigurationObjects);

        System.out.println("Container contents: " + container.getAsString());
        Class[] controllers = discoverer.getClassesAnnotatedWith(Controller.class);
        Class[] filters = discoverer.getSubClasses(Filter.class);
        System.out.println("Initializing router with controllers and filters: " + controllers.length + ", " + filters.length);
        
        // Build router with discovered controllers and filters
        router = new Router(container);
        router.init(controllers, filters);

        for (int i = 0; i < router.routes.size(); i++) {
            Route r = (Route) router.routes.get(i);
            System.out.println(r.getAsString());
        }

    }
}
