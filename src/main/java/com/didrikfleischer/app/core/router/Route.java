package com.didrikfleischer.app.core.router;

import com.didrikfleischer.app.core.di.DIContainer;
import com.didrikfleischer.app.core.di.annotations.PathVariable;
import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;
import com.didrikfleischer.app.core.router.ControllerException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.ArrayList;

/**
 * The route needs information to match againts URLs+methods
 * The route needs to know what handler to call
 * The route needs to know how to call the handler, what arguments it takes...
 * The route can resolve the needed objects by using the DIContainer...
 * 
 */

public class Route {
    public String url;
    private Pattern urlPattern;
    public String method = "GET";
    private Object controller;
    private Method handler;
    private Boolean handlerReturnsBody = false; 
    // private Properties urlParams;
    public String urlParam;
    public DIContainer container;


    public Route(DIContainer container) {
        this.container = container;
    }

    public void setHandlerReturnsBody(Boolean value) {
        this.handlerReturnsBody = value;
    }

    public void setHandler(Object controller, Method handler) { // maybe method should be string...
        // instansiate controller

        this.controller = controller;


        this.handler = handler;
    }

    public void setPattern(String requestMappingValue, String method) {
        this.url = requestMappingValue;

        // generate regex to match (omit to later, just specify the regex in the controller)
        // I only accept one level of url parameters and they must be numbers or chars.
        // "/jokes/{index}" -> "/jokes/(\d+)"
        this.urlPattern = Pattern.compile(requestMappingValue);

        this.method = method;
    }

    public Boolean match(Request request) {
        
        if (!request.method.equals(this.method)) {
            return false;
        }
        
        Matcher matcher = this.urlPattern.matcher(request.url);
        // System.out.println("Url Pattern: " + this.url);
        // System.out.println("Request url: " + request.url);
        
        if (matcher.matches()) { // this is enough for a route match
            // System.out.println("matcher.find() returned true for: " + request.url);
            if (matcher.groupCount() >= 1) {
                // System.out.println("Group info: " + matcher.groupCount() + " " + matcher.group(0) + " " + matcher.group(1));
                this.urlParam = matcher.group(1);
            }
        } else {
            return false;
        }
        System.out.println("Route match for: " + this.method + this.url);
        return true;
    }

    /**
     * Execute the route handler. 
     * Limitations: 
     *  - Only string and int annotated parms supported 
     *  - Only one route parameter supported at a time
     * @param request
     * @param response
     * @return Response
     */
    public Response execute(Request request, Response response) {
        System.out.println("Route EXECUTE CALLED!");
        try {
            Annotation[][] annotations = this.handler.getParameterAnnotations();
            Class[] parameterTypes = this.handler.getParameterTypes();
            ArrayList parameterInstances = new ArrayList();
            int parameterIndex = 0;

            for (Annotation[] paramAnnotations : annotations) {
                System.out.println(paramAnnotations.length + " annotations");
                Class parameterType = parameterTypes[parameterIndex];
                if (paramAnnotations.length == 0) {
                    Object paramInstance = this.container.getBean(parameterType);
                    parameterInstances.add(paramInstance);
                } else {
                    System.out.println("Parameter type of annotated param: " + parameterType.getName());
                    Annotation paramAnnotation = paramAnnotations[0];
                    if (paramAnnotation.annotationType() == PathVariable.class) {
                        if (parameterType.getName() == "java.lang.String") { // only deal with int and string.
                            parameterInstances.add(this.urlParam);
                        } else if (parameterType.getName() == "int") {
                            parameterInstances.add(Integer.parseInt(this.urlParam));
                        }
                    }
                }
                parameterIndex++;
            }

            Object body = this.handler.invoke(this.controller, parameterInstances.toArray()); // dont know the return type

            if (this.handlerReturnsBody) {
                response.setBodyWithoutType(body);
            }
            // if handler does not return response body, do nothing. 
            // Later the return value could be used to do redirect etc.

        } catch (InvocationTargetException wrapperEx) {
            //ControllerException ex = (ControllerException) wrapperEx.getTargetException();
            Throwable ex = wrapperEx.getTargetException();
            System.out.println("Some execption in a controller method: ");
            ex.printStackTrace();
            response.setStatus(500);
            response.setBody("Some exception in a controller method for url: " + this.url, "text");

            // response.setStatus(ex.getStatusCode());
            // response.setBody(ex.getResponseBody(), "text");
            return response;
        } catch(Exception ex) {
            ex.printStackTrace();
            response.setStatus(500);
            response.setBody("Error in controller handler!", "text");
            return response;
        }

        return response;
    }


    public String getAsString() {
        String res = "Route for " + this.url + " - " + this.method + "\n";
        res += "   - Controller: " + this.controller.getClass().getName() + "\n";
        res += "   - Method: " + this.method + "\n";
        res += "   - Handler returns body: " + this.handlerReturnsBody + "\n";
        // res += "   - URL pattern: " + this.urlPattern.pattern() + "\n";
        return res;

    }

}