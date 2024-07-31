package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;


public class RequestParser {
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String initialLine = reader.readLine();
        
        if (initialLine == null || initialLine.isEmpty()) {
            throw new IOException("Empty request line");
        }

        String[] initialLineParts = initialLine.split(" ");
        if (initialLineParts.length != 3) {
            throw new IOException("Invalid request line: " + initialLine);
        }

        String command = initialLineParts[0];
        String uri = initialLineParts[1];
        String[] uriParts;
        Map<String, String> parameters = new HashMap<>();
        
        
        int questionMarkIndex = uri.indexOf('?');
        if (questionMarkIndex != -1) {
            uriParts = uri.substring(1, questionMarkIndex).split("/");
            String paramString = uri.substring(questionMarkIndex + 1);
            String[] paramPairs = paramString.split("&"); 
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("="); 
                if (keyValue.length != 2)
                    throw new IllegalArgumentException("Invalid HTTP parameter: " + pair);
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                parameters.put(key, value);
            }
        } else {
            uriParts = uri.substring(1).split("/"); // If no "?" then split segments by "/"
        }
        

        // Skip headers
        while (reader.ready() && (initialLine = reader.readLine()) != null && !initialLine.isEmpty()) {}
        
        //while (reader.ready() && (initialLine = reader.readLine()) != null && !initialLine.contains("------")) {}

        while (reader.ready() && (initialLine = reader.readLine()) != null && !initialLine.isEmpty() && initialLine.contains("=")) {
            
        	if (initialLine.contains("filename=")) { 
                String[] params = initialLine.split(" ");
                initialLine = initialLine.split(" ")[params.length - 1];
            }
                
            String[] keyValue = initialLine.split("=");
            if (keyValue.length != 2)
                throw new IllegalArgumentException("Invalid HTTP parameter: " + initialLine);
            parameters.put(keyValue[0], keyValue[1].substring(0, keyValue[1].length()));            
        }
        
        // Content part
        StringBuilder bodyBuilder = new StringBuilder();
        while (reader.ready() && (initialLine = reader.readLine()) != null && !initialLine.isEmpty()) {
            bodyBuilder.append(initialLine).append("\n");
        }
        
        byte[] content = bodyBuilder.toString().getBytes();

        System.out.println("b");
        System.out.println(command);
        System.out.println(uri);
        System.out.println(parameters);
        System.out.println(content.toString());
        return new RequestInfo(command, uri, uriParts, parameters, content);
        
        
    }
	
	// the inner class for the methods we receive in the http requests
	public static class RequestInfo {
		private final String command;
		private final String uri;
		private final String[] uri_parts;
		private final Map<String, String> parameters;
		private final byte[] content;
		
		public RequestInfo(String command, String uri, String[] uri_parts, Map<String, String> parameters, byte[] content) {
			this.command = command;
			this.uri = uri;
			this.uri_parts = uri_parts;
			this.parameters = parameters;
			this.content = content;
		}
		
		// getters
		public String getHttpCommand() {
	        return command;
	    }

	    public String getUri() {
	        return uri;
	    }

	    public String[] getUriSegments() {
	        return uri_parts;
	    }

	    public Map<String, String> getParameters() {
	        return parameters;
	    }

	    public byte[] getContent() {
	        return content;
	    }
	}
}