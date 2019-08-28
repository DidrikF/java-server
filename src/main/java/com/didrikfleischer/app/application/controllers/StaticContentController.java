package com.didrikfleischer.app.application.controllers;

import com.didrikfleischer.app.test.beans.Fido;
import com.didrikfleischer.app.test.beans.Animal;
import com.didrikfleischer.app.core.di.annotations.*;
import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;
import com.didrikfleischer.app.core.router.ControllerException; // throws ControllerExceptoin(body, statusCode)

import java.util.Vector;
import java.util.Iterator;


@Controller
public class StaticContentController {
    @RequestMapping(value="\\/", method="GET")
    public void getIndex(Request request, Response response) {
        String fileName = "index.html";
        System.out.println("Requested File: " + fileName);
        response.setFile(fileName);
    }
    @RequestMapping(value="\\/[\\w\\.]+\\.(json|html|htm|js|css|ico|png|jpeg|jpg|svg){1}$", method="GET")
    public void getFile(Request request, Response response) {
        // String fileName = request.url.replace("/", "");
        System.out.println("Requested File: " + request.url);
        response.setFile(request.url);
    }
    @RequestMapping(value="\\/static\\/[\\w\\.\\/]+\\.(json|html|htm|js|css|ico|png|jpeg|jpg|svg){1}$", method="GET")
    public void getFilesFromStatic(Request request, Response response) {
        // String fileName = request.url.replace("/", "");
        System.out.println("Requested File: " + request.url);
        System.out.println("MATCHED CRAZY REGEX");
        response.setFile(request.url);
    }
}


/*

// This will reference one line at a time
String line = null;
String body = ""; 
try {
    // FileReader reads text files in the default encoding.
    FileReader fileReader = 
        new FileReader(fileName);

    // Always wrap FileReader in BufferedReader.
    BufferedReader bufferedReader = 
        new BufferedReader(fileReader);


    while((line = bufferedReader.readLine()) != null) {
        body += line;
    }   

    // Always close files.
    bufferedReader.close();         
}
catch(FileNotFoundException ex) {
    System.out.println(
        "Unable to open file '" + 
        fileName + "'");                
}
catch(IOException ex) {
    System.out.println(
        "Error reading file '" 
        + fileName + "'");                  
    // Or we could just do this: 
    // ex.printStackTrace();
}

response.setStatus(200);

String contentType = getContentType(fileName);

response.setHeader("Content-Type", contentType);
response.setBody(body, "text");

*/