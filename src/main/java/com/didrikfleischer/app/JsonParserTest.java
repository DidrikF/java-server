package com.didrikfleischer.app;

import com.didrikfleischer.app.core.parser.JsonParser;
import java.util.HashMap;

public class JsonParserTest {
    public static void main(String[] args) {

        String json = "{\"key1\": \"value1\", \"key2\": \"value2\"}";

        String json2 = "";
        json2 += "{\n";
        json2 += "\"key1\": \"value1\",\n";
        json2 += "\"key2\": \"value2\",\n";
        json2 += "\"numberKey\": 1,\n"; 
        json2 += "\"arrayKey\": [\"hello\", 123]\n";
        json2 += "}";

        JsonParser parser = new JsonParser(json2);
        HashMap<String, Object> result = parser.parse();

        System.out.println(result.toString());
    }
}