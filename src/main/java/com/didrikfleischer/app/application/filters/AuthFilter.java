package com.didrikfleischer.app.application.filters;

import com.didrikfleischer.app.core.di.annotations.Component;
import com.didrikfleischer.app.core.di.annotations.Order;
import com.didrikfleischer.app.core.router.Filter;
import com.didrikfleischer.app.core.router.FilterException;
import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;

@Component
@Order(index=2)
public class AuthFilter implements Filter {
    public void doFilter(Request request, Response response) throws FilterException {

        String key = request.getHeader("Authorization");
        if (key != "SECRET_KEY") {
            // throw new FilterException("NOT AUTHORIZED");
            System.out.println("AuthFilter executed! Secret Key did not match.");
        }
    }
}