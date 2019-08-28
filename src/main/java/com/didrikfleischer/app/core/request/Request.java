package com.didrikfleischer.app.core.request;

// import com.didrikfleischer.app.core.parser.JsonParser;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Hashtable;
import java.util.Arrays;
import java.util.Set;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.util.HashMap; // prefered over HashTable if thread synchronization is not needed // is a more mature and advanced version of HashTable



public class Request {
    // How to differentiate between local and isntance variables?

    // These are called member variables.
    public String originalRequest; 
    public String startLine;
    public String method;
    public String url; // are null when initialized.
    public String protocol; 

    public Hashtable<String, String> headers;
    
    public String rawBody;
    public String bodyType; // use this to know what body-property to access; 
    public Object body; // Only a very simple JSON structure is allowed. (use json.simple for more advanced cases)

    public static JSONParser jsonParser = new JSONParser();



    // If we cant execute the constructor secessfully, we simply wont handle the request.
    public Request(InputStream input) throws IOException, NullPointerException, IOException, HttpFormatException, Exception {        
        this.originalRequest = this.readRequestStreamToString(input);
        this.parseRequest(originalRequest);
    }

    public String getHeader(String header) {
        return this.headers.get(header);
    }

    public String getBodyAsString() {
        if (this.rawBody == null || this.rawBody.length() == 0) {
            return "";
        }
        return this.rawBody; // accesses the class variable not the instance?
    }


    private String readRequestStreamToString(InputStream input) throws IOException, NullPointerException {
        byte[] buffer = new byte[10000]; // array of bytes which can hold 8KB of data
        int total = input.read(buffer); // returns -1 for some reason? I need to length of the message!
        String request = new String(Arrays.copyOfRange(buffer, 0, 10000));
        return request;
    }

    private void parseRequest(String originalRequest) throws IOException, HttpFormatException, Exception {
        // read line by line and enterprete the content of the request.
        // Make all relevant fields available as properties on the Request object
        // use specialized parsers for the reqeust body to deal with JSON, multipart form-data, text etc.
        // it is generally adviced to wrap any read request on a Reader in a BufferedReader. 
        // I think you avoid translating the bytes to characters when reading
        BufferedReader reader = new BufferedReader(new StringReader(originalRequest)); // this is mostly for efficiency purposes it seems

        setStartLine(reader.readLine());

        // parse headers:
        headers = new Hashtable<String, String>();
        String headerLine = reader.readLine();
        while (headerLine != null && headerLine.length() > 0) {
            appendHeaderParameter(headerLine);
            headerLine = reader.readLine();
        }

        String bodyLine = reader.readLine(); // returns null when the end of the stream is reached.
        String rawBody = "";
        while (bodyLine != null) {
            rawBody += bodyLine;
            bodyLine = reader.readLine();
        }

        rawBody = rawBody.trim();

        // parse the body (which is optional); Determining if there was a body based on content of rawBody.
        if ((rawBody != null) && (rawBody.length() > 0)) { // this.method != "GET" && 
            this.rawBody = rawBody;
            parseBody(rawBody, this.headers.get("content-type"));
        }
    }

    private void setStartLine(String startLine) throws Exception {
        this.startLine = startLine;
        System.out.println("startLine: " + startLine);
        String[] parts = startLine.split(" ");

        if (parts.length < 3) {
            throw new Exception("Start line in HTTP request is mallformed");
        }

        this.method = parts[0];
        this.url = parts[1];
        this.protocol = parts[2];
    }

    private void appendHeaderParameter(String headerLine) {
        // System.out.println("Header line: " + headerLine);
        String[] keyAndValue = headerLine.split(":");
        for (int i = 0; i < keyAndValue.length; i++) {
            keyAndValue[i] = keyAndValue[i].trim().toLowerCase();
        }

        this.headers.put(keyAndValue[0], keyAndValue[1]);
    }

    private void parseBody(String body, String contentType) throws Exception {
        System.out.println("Content type: " + contentType);
        if (contentType.compareTo("application/json;charset=utf-8") == 0) {
            // JsonParser jsonParser = new JsonParser(body);
            // this.jsonBody  = jsonParser.parse();
            // REPLACE WITH SIMPLE JSON
            this.body = jsonParser.parse(body);
        } else if (contentType.compareTo("multipart/form-data") == 0) {
            // etc.
        } else {
            // the content-type key was not found on headers (value of null) or the format is not supported.
            System.out.println(getAsString());
            throw new Exception("Unsuported content-type with value: "  + (String) contentType);
        }
    }

    public String getAsString() {
        String request = "";

        request += "Method: " + this.method;
        request += "\nUrl: " + this.url;
        request += "\nProtocol: " + this.protocol;
        request += "\nHeaders: ";
        Set<String> keys = this.headers.keySet();
        for(String key: keys){
            request += "\n - " + key + ": " + this.headers.get(key);
        }
        request += "\nBody Type: " + this.bodyType;
        request += "\nRaw Body: " + this.rawBody;

        return request;
    }
}

/*

requests containing a message-body MUST include a valid Content-Length header field unless the 
server is known to be HTTP/1.1 compliant. If a request contains a message-body and a Content-Length 
is not given, the server SHOULD respond with 400 (bad request) if it cannot determine the length of 
the message, or with 411 (length required) if it wishes to insist on receiving a valid Content-Length.

GET / HTTP/1.1
Host: localhost:8080
Connection: keep-alive
Cache-Control: max-age=0
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36
Sec-Fetch-Mode: navigate
Sec-Fetch-User: ?1
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,anything/anything;q=0.8,application/signed-exchange;v=b3
Sec-Fetch-Site: none
Accept-Encoding: gzip, deflate, br
Accept-Language: nb-NO,nb;q=0.9,no;q=0.8,nn;q=0.7,en-US;q=0.6,en;q=0.5


*/