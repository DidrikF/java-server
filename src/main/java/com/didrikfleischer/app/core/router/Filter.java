package com.didrikfleischer.app.core.router;

import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;


public interface Filter {
    public void doFilter(Request request, Response response) throws FilterException;
}