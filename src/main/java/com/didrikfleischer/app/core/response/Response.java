package com.didrikfleischer.app.core.response;

// import com.didrikfleischer.app.core.parser.JsonParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.io.File;



public class Response {
    OutputStream output;
    BufferedOutputStream outputBinary;

    String protocol;
    String status;

    Properties headers;

    Object body;
    String bodyType;

    byte[] fileData;
    int fileLength;


    static final File WEB_ROOT = new File("frontend/build");
    
    public Response(OutputStream output, Properties defaultHeaders){
        System.out.println("Working Directory = " +
              System.getProperty("user.dir"));
        this.output = output;
        this.outputBinary = new BufferedOutputStream(output);
        this.protocol = "HTTP/1.1";
        this.headers = defaultHeaders; // deafult headers need not to depend on the request.
    }

    
    public void send() throws IOException {
        // update to send both files and content!
        String response = composeResponse(); // update to deal with files, in case of a file, I just want the status and headers and separator
        System.out.println("SEND# " + response);
        this.output.write(response.getBytes());
        this.output.flush();
        System.out.println("SEND# body type: " + this.bodyType);
        if (this.bodyType != null && this.bodyType.equals("file")) {
            this.outputBinary.write(this.fileData, 0 , this.fileLength);
            this.outputBinary.flush();
        }
        this.outputBinary.close(); // ???
    }

    public void setFile(String fileName) {
        // prepare the response, so that the send method can write the file to the output stream
        try {
            File file = new File(WEB_ROOT, fileName);
            this.fileLength = (int) file.length();
            this.fileData = this.readFileData(file, this.fileLength);

            this.bodyType = "file"; // tell compose response and send how to compose and send the response.
            String contentType = this.getContentType(fileName);
            this.setStatus(200);
            this.setHeader("Content-Type", contentType);
            this.setHeader("Content-length", Integer.toString(this.fileLength));
        } catch(FileNotFoundException ex) {
            this.setStatus(404);
            this.setBody("File not found", "text");
            this.setHeader("Content-Type", "text/plain");
        } catch(IOException ex) {
            this.setStatus(500);
            this.setBody("File reading error", "text");
            this.setHeader("Content-Type", "text/plain"); 
        } catch(Exception ex) {
            System.out.println("Unknown error in setFile: ");
            ex.printStackTrace();
            this.setStatus(500);
            this.setBody("Unknown error in setFile", "text");
            this.setHeader("Content-Type", "text/plain"); 
        }
    }

    public byte[] readFileData(File file, int fileLength) throws FileNotFoundException, IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if(fileIn != null) { // Another way to do cleanup without catching anything specific... (?)
                fileIn.close();
            }
        }
        return fileData;
    }

    public void getHeader(String header) {
        this.headers.getProperty(header);
    }

    public void setHeader(String header, String value) {
        this.headers.put(header, value);
    }

    public void setStatus(Integer statusCode) {
        switch(statusCode) {
            case 200:
                this.status = "200 OK";
                break;
            case 201:
                this.status = "201 Created";
                break;
            case 307:
                this.status = "307 Temporary Redirect";
                break;
            case 308:
                this.status = "308 Permanent Redirect";
                break;                
            case 400:
                this.status = "400 Bad Request";
                break;
            case 401:
                this.status = "401 Unauthorized";
                break;
            case 403: 
                this.status = "403 Forbidden";
                break;
            case 404:
                this.status = "404 Not Found";
                break;
            case 500:
                this.status = "500 Internal Server Error";
                break;
            case 501:
                this.status = "501 Not Implemented";
                break;
            default:
                this.status = "500 Internal Server Error";
        }
    }

    public void setBody(Object body, String bodyType) {
        this.body = body;
        this.bodyType = bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public void setBodyWithoutType(Object body) {
        this.body = body;
    }

    private String getBodyAsString() {
        // The body is in format this.bodyType and is should be returned in format this.headers.get("Content-Type").
        System.out.println("Get body as string from type: " + this.bodyType);

        if (this.bodyType == null) {
            this.setStatus(404);
            return "Response body type is not set";
        }

        if(this.bodyType.equals("json")) { 
            // JsonParser jsonParser = new JsonParser(this.body);
            // String preparedBody = jsonParser.toJsonString();
            JSONObject jsonBody = (JSONObject) this.body;
            String jsonString = jsonBody.toJSONString();
            return jsonString;

        } else if (this.bodyType.equals("text")) {
            return (String) this.body;
        } else if (this.bodyType.equals("file")) {
            return "";
        } 

        this.setStatus(404);
        return  "Response.getBodyAsString() does not know bodyType: " + this.bodyType;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    // Need to implement
    public String composeResponse() {
        String response = "";
        Set<Object> headerKeys = this.headers.keySet();
        Iterator<Object> headerKeysIterator = headerKeys.iterator();
        String headerKey;
        String body = this.getBodyAsString();

        response += this.protocol + " " + this.status;
        while (headerKeysIterator.hasNext()) {
            response += "\n";
            headerKey = (String) headerKeysIterator.next();
            response += headerKey + ": " + this.headers.getProperty(headerKey);
        }
        response += "\r\n\r\n"; // one \n to much!
        response += body;

        return response;

        // return "HTTP/1.1 200 OK\r\n\r\nNeed to implement Response.composeResponse";
    }

    public String getContentType(String fileName) {
        String[] parts = fileName.split("\\.");

        String extention = parts[parts.length - 1];

        switch(extention) {
            case "html":
            case "htm":
                return "text/html";
            case "js": 
                return "application/javascript";
            case "css":
                return "text/css";
            case "svg":
                return "image/svg+xml";
            case "json":
                return "application/json";
            case "png":
                return "image/png";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            case "ico":
                return "image/x-icon";
            default:
                return "text/plain";
        }
    }
}