package com.didrikfleischer.app.core.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class JsonParser {
    /**
     * The parser only supports one level deep JSON documents of strings, ints and arrays of strings and ints.
     */
    private String jsonString;
    private Object javaJson;
    
    public JsonParser(String jsonString) {
        this.jsonString = jsonString;
    }

    public JsonParser(Object json) {
        this.javaJson = json;
    }

    public HashMap<String, Object> parse() {
        HashMap<String, Object> json = new HashMap<String, Object>();

        Pattern p = Pattern.compile("\"([a-z0-9_-]+)\": ((\"?([a-z0-9_-]+)\"?)|(\\[.*\\]))", Pattern.CASE_INSENSITIVE); //\"([a-z0-9_-]+)\": \"?([\\[\\]a-z0-9_-]+)\"?
        Matcher m = p.matcher(this.jsonString);
        System.out.println("Original String: " + this.jsonString);
        
        while (m.find()) {
            String key = m.group(1);
            String value = m.group(2);
            System.out.println(key + ": " + value);

            // Parse array:
            if (value.charAt(0) == '[' && value.charAt(value.length() - 1) == ']') {
                String elementsString = value.substring(1, value.length() - 2);
                String[] elements = elementsString.split(", ");

                ArrayList<Object> parsedValue = new ArrayList<Object>();
                String element;
                for(int i = 0; i < elements.length; i++) {
                    element = elements[i];
                    try {
                        Integer parsedElement = Integer.parseInt(element);
                        parsedValue.add(parsedElement);
                    } catch (NumberFormatException ex) {
                        // It is a string
                        parsedValue.add(element);
                    }
                }
                json.put(key, parsedValue);
                continue;
            }

            // Parse Int or keep as string
            try {
                Integer parsedValue = Integer.parseInt(value);
                json.put(key, parsedValue);
                continue;
            } catch (NumberFormatException ex) {
                // It is a string
                json.put(key, value);
            }
        }

        this.javaJson = json;

        return json;
    }

    public String toJsonString() {

        return "NEED TO IMPLEMENT OBJECT TO JSON METHOD";
    }

    public String toJson(Object obj) {

        return "";
    }
}

/**
 * {"key1": "value1", "key2": "value2", "numberKey": 1} // I want to parse this kind of simple json object.
 */