package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> headers = new HashMap<>();

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
            uriParts = uri.substring(1).split("/");
        }

        // Read headers
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            int colonIndex = headerLine.indexOf(":");
            if (colonIndex != -1) {
                String headerName = headerLine.substring(0, colonIndex).trim();
                String headerValue = headerLine.substring(colonIndex + 1).trim();
                headers.put(headerName, headerValue);
            }
        }

        // Added parsing for multipart form data
        boolean isMultipart = false;
        String boundary = null;

        // Check for multipart/form-data boundary
        if (headers.containsKey("Content-Type") && headers.get("Content-Type").startsWith("multipart/form-data")) {
            isMultipart = true;
            boundary = headers.get("Content-Type").split("boundary=")[1];
        }

        byte[] content = null;

        if (isMultipart) {
            StringBuilder bodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line).append("\n");
            }
            String body = bodyBuilder.toString();

            String[] parts = body.split("--" + boundary);
            for (String part : parts) {
                if (part.contains("Content-Disposition")) {
                    String[] dispositionLines = part.split("\r\n");
                    String name = null;
                    String filename = null;
                    StringBuilder partContent = new StringBuilder();

                    for (String dispLine : dispositionLines) {
                        if (dispLine.startsWith("Content-Disposition")) {
                            String[] items = dispLine.split("; ");
                            for (String item : items) {
                                if (item.startsWith("name=")) {
                                    name = item.split("=")[1].replace("\"", "");
                                } else if (item.startsWith("filename=")) {
                                    filename = item.split("=")[1].replace("\"", "");
                                }
                            }
                        } else if (!dispLine.isEmpty()) {
                            partContent.append(dispLine).append("\n");
                        }
                    }

                    if (filename != null) {
                        parameters.put("filename", filename);
                        parameters.put("fileContent", partContent.toString());
                    }
                }
            }

            if (parameters.containsKey("fileContent")) {
                content = parameters.get("fileContent").getBytes(StandardCharsets.UTF_8);
            }
        } else {
            StringBuilder bodyBuilder = new StringBuilder();
            while (reader.ready() && (initialLine = reader.readLine()) != null && !initialLine.isEmpty()) {
                bodyBuilder.append(initialLine).append("\n");
            }
            content = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }

        System.out.println("Received command: " + command);
        System.out.println("Received uri: " + uri);
        System.out.println("Received parameters: " + parameters);
        System.out.println("Received content length: " + (content != null ? content.length : "null"));

        return new RequestInfo(command, uri, uriParts, parameters, content);
    }

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
