package com.didrikfleischer.app.core.router;

import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;
import com.didrikfleischer.app.core.di.DIContainer;
import com.didrikfleischer.app.core.di.annotations.*;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/*
The router executes all matching filters
The router only matches the first matching route per request.
*/

/* Need to build routes and filters based on the following annotations
"code.di.annotations.Order",
"code.di.annotations.RequestMapping",
"code.di.annotations.RequestBody",
*/

public class Router {
    public Vector filterInstances;
    public Vector routes;
    public DIContainer container;

    public Router(DIContainer container) {
        this.filterInstances = new Vector();
        this.routes = new Vector();
        this.container = container;
    }

    public void init(Class[] controllers, Class[] filters) {
        // inspect controllers/filters and its methods annotations to build Routes/Filtes
        Arrays.sort(filters, (Class a, Class b)->{
            if (a.isAnnotationPresent(Order.class) && b.isAnnotationPresent(Order.class)) {
                Order orderA = (Order) a.getAnnotation(Order.class);
                Order orderB = (Order) b.getAnnotation(Order.class);
                return orderA.index() - orderB.index();
            }
            return 0;
        });

        Filter filterInstance;
        for (Class filter: filters) {
            // I could istantiate here the filter here, to make the code more efficient.
            try {
                Constructor[] constructors = filter.getDeclaredConstructors();
                Constructor constructor = constructors[0];
                Class[] argumentTypes = constructor.getParameterTypes();
                Object[] argumentInstances = this.container.getMultipleBeans(argumentTypes);
                filterInstance = (Filter) constructor.newInstance(argumentInstances);
                this.addFilter(filterInstance);
            } catch(Exception ex) {
                System.out.println("Failed to make filter for some of many reasons.");
            }
        }
        
        for (Class controller: controllers) {
            // Build controller instance:
            try {
                Constructor[] constructors = controller.getDeclaredConstructors();
                Object controllerInstance;
                Constructor constructor = constructors[0];
                Class[] argumentTypes = constructor.getParameterTypes();
                Object[] argumentInstances = this.container.getMultipleBeans(argumentTypes);
                controllerInstance = constructor.newInstance(argumentInstances);
                       
                // Build routes for each method annotated with RequestMapping
                Method[] methods = controller.getDeclaredMethods();
                for (Method method : methods) {
                    List<Annotation> annotations = Arrays.asList(method.getAnnotations());
                    System.out.println("Annotaions on controller: " + annotations.get(0));
                    if (method.isAnnotationPresent(RequestMapping.class)) { // this method is a handler for a route.
                        Route route = new Route(this.container);
                        route.setHandler(controllerInstance, method);
                        for(Annotation annotation: annotations) {
                            if (annotation.annotationType() == RequestMapping.class) {
                                RequestMapping reqMap = (RequestMapping) annotation;
                                route.setPattern(reqMap.value(), reqMap.method());
                            } else if (annotation.annotationType() == ResponseBody.class) {
                                route.setHandlerReturnsBody(true);
                            }
                        }
                        this.addRoute(route);
                    }
                }
            } catch(Exception ex) {
                System.out.println("Failed to make controller route for some of many possible reasons.");
                ex.printStackTrace();

            }
        }
    }


    private void addRoute(Route route) {
        this.routes.add(route);
    }

    private void addFilter(Filter filter) {
        this.filterInstances.add(filter);
    }

    public Response handleRequest() {
        Iterator filterIterator = this.filterInstances.iterator();
        Request request = (Request) this.container.getBean(Request.class);
        Response response = (Response) this.container.getBean(Response.class);

        while (filterIterator.hasNext()) {
            Filter filterInstance = (Filter) filterIterator.next(); // has a doFilter method...
            try {
                // Now I new up on each request, not optimal...
                // Get first doFilter method on filter-instance.
                Method[] filterMethods = filterInstance.getClass().getDeclaredMethods();
                Method doFilter;
                for (Method method : filterMethods) {
                    if (method.getName() == "doFilter") {
                        doFilter = method;
                        Class[] parameterTypes = doFilter.getParameterTypes();
                        Object[] parameterInstances = this.container.getMultipleBeans(parameterTypes);

                        // Should retreve the same objects on every run, modifying the same Request and Response objects...
                        doFilter.invoke(filterInstance, parameterInstances);
                        break;
                    }
                }
                
            } catch(InvocationTargetException wrapperEx) { // it cannot know if this exception is thrown a controller
                FilterException ex = (FilterException) wrapperEx.getTargetException();
                response.setStatus(ex.getStatusCode());
                response.setBody(ex.getResponseBody(), "text");
                return response;
            } catch(SecurityException ex) {

            } catch(IllegalAccessException ex) {

            } catch(IllegalArgumentException ex) {

            } catch(Exception ex) {
                ex.printStackTrace();
                response.setStatus(404);
                response.setBody("Building/argument resolving/calling of some filter failed!", "text");
                return response;
            } 
        }

        Iterator routeIterator = this.routes.iterator();
        while(routeIterator.hasNext()) {
            Route route = (Route) routeIterator.next();
            if (route.match(request)) {
                System.out.println("ROUTE MATCHED!");
                response = route.execute(request, response);
                return response;
            }
        }
        return response;
    }
}



/* Something to aim for...

@RestController
public class RestAnnotatedController {
    @GetMapping(value = "/annotated/student/{studentId}")
    public Student getData(@PathVariable Integer studentId) {
        Student student = new Student();
        student.setName("Peter");
        student.setId(studentId);
 
        return student;
    }
}
*/