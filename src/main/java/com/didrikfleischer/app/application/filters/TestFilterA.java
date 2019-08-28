package com.didrikfleischer.app.application.filters;

import com.didrikfleischer.app.core.di.annotations.Component;
import com.didrikfleischer.app.core.di.annotations.Order;
import com.didrikfleischer.app.core.router.Filter;
import com.didrikfleischer.app.core.router.FilterException;
import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;

@Component
@Order(index=0)
public class TestFilterA implements Filter {
    public void doFilter(Request request, Response response) throws FilterException {
        System.out.println("TestFilterA executed!");
    }
}