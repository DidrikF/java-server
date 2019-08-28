package com.didrikfleischer.app.application.controllers;

import java.util.Vector;
import java.util.Iterator;

import com.didrikfleischer.app.test.beans.Fido;
import com.didrikfleischer.app.core.di.annotations.*;
import com.didrikfleischer.app.core.request.Request;
import com.didrikfleischer.app.core.response.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


@Controller
public class JokeController {
    private JSONObject jokes;

    public JokeController() {
        // super(); // this call is implisit, you only neeed to call it to specify the arguments on the parent contstructor
        JSONArray list = new JSONArray();
        this.jokes = new JSONObject();
        this.jokes.put("list", list);

        JSONObject joke1 = new JSONObject();
        JSONObject joke2 = new JSONObject();
        JSONObject joke3 = new JSONObject();
        JSONObject joke4 = new JSONObject();
        joke1.put("author", "Pete Johnson");
        joke1.put("joke", "Today at the bank, an old lady asked me to help check her balance. So I pushed her over.");
        joke1.put("votes", 0L);
        joke2.put("author", "Kathy Hansen");
        joke2.put("joke", "I bought some shoes from a drug dealer. I don't know what he laced them with, but I've been tripping all day.");
        joke2.put("votes", 0L);
        joke3.put("author", "Joe Blow");
        joke3.put("joke", "I told my girlfriend she drew her eyebrows too high. She seemed surprised.");
        joke3.put("votes", 0L);
        joke4.put("author", "John Doe");
        joke4.put("joke", "My dog used to chase people on a bike a lot. It got so bad, finally I had to take his bike away.");
        joke4.put("votes", 0L);

        list.add(joke1);
        list.add(joke2);
        list.add(joke3);
        list.add(joke4);
    }

    @RequestMapping(value="\\/jokes", method="GET")
    @ResponseBody
    public Object getJokes(Request request, Response response, Fido fido) {
        response.setStatus(200);
        response.setHeader("Content-Type", "application/json");
        response.setBodyType("json");
        System.out.println("JSON String jokes: ");
        System.out.println(this.jokes.toJSONString());
        return this.jokes;
    }

    @RequestMapping(value="\\/jokes\\/(\\d+)", method="GET")
    @ResponseBody
    public Object getJoke(Request request, Response response, @PathVariable(value="index") int index) {
        JSONObject joke = (JSONObject) ((JSONArray) this.jokes.get("list")).get(index);

        response.setStatus(200);
        response.setHeader("Content-Type", "application/json");
        response.setBodyType("json");
        return joke;
    }
    
    @RequestMapping(value="\\/jokes", method="POST")
    public void postJoke(Request request, Response response) {
        JSONObject joke = (JSONObject) request.body;
        joke.put("votes", 0L);
        ((JSONArray) this.jokes.get("list")).add(joke);

        response.setStatus(201);
        response.setHeader("Content-Type", "application/json");
        response.setBody(this.jokes, "json");
    }

    @RequestMapping(value="\\/votes", method="POST")
    public void voteOnJoke(Request request, Response response) {
        JSONObject vote = (JSONObject) request.body;
        long jokeIndex = (long) vote.get("index");
        long voteValue = (long) vote.get("vote");

        JSONObject joke = (JSONObject) ((JSONArray) this.jokes.get("list")).get(Math.toIntExact(jokeIndex));
        long voteCount = (long) joke.get("votes");
        joke.put("votes", voteCount + voteValue); // update the referenced object


        response.setStatus(201);
        response.setHeader("Content-Type", "application/json");
        response.setBody(this.jokes, "json");
    }

}


/**
 * In addition to processing an annotation using an annotation processor, a Java programmer 
 * can write their own code that uses reflections to process the annotation. Java SE 5 supports 
 * a new interface that is defined in the java.lang.reflect package. This package contains the 
 * interface called AnnotatedElement that is implemented by the Java reflection classes including
 *  Class, Constructor, Field, Method, and Package. The implementations of this interface are used 
 * to represent an annotated element of the program currently running in the Java Virtual Machine. 
 * This interface allows annotations to be read reflectively.
 * 
 */