package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.RequestParser.RequestInfo;

public class HtmlLoader implements Servlet {

	private final String htmlDirectory;

	public HtmlLoader(String htmlDirectory) {
		this.htmlDirectory = htmlDirectory;
	}

	@Override
	public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
		// Check if the HTTP method is GET
		if (!"GET".equals(ri.getHttpCommand())) {
			toClient.write("HTTP/1.1 405 Method Not Allowed\r\n\r\n".getBytes());
			toClient.flush();
			return;
		}

		// Extract URI segments from the request
		String[] URIsegments = ri.getUriSegments();
		if (URIsegments.length < 2) {
			toClient.write("HTTP/1.1 400 Bad Request\r\n\r\nInvalid request".getBytes());
			toClient.flush();
			return;
		}

		// Get the filename from the URI segments
		String fileName = URIsegments[1];
		Path filePath = Paths.get(htmlDirectory, fileName);

		// Check if the file exists and is not a directory
		if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
			byte[] content = Files.readAllBytes(filePath);
			String contentType = getMIMEtype(fileName);

			// Construct and send the HTTP response header
			String response = "HTTP/1.1 200 OK\r\n" +
					"Content-Type: " + contentType + "\r\n" +
					"Content-Length: " + content.length + "\r\n" +
					"\r\n";

			toClient.write(response.getBytes());
			toClient.write(content);
			toClient.flush();
		} else {
			// Send a 404 Not Found response if the file does not exist
			String errorContent = "<html><body><h1>404 Not Found</h1><p>File not found: " + fileName
					+ "</p></body></html>";
			String response = "HTTP/1.1 404 Not Found\r\n" +
					"Content-Type: text/html\r\n" +
					"Content-Length: " + errorContent.length() + "\r\n" +
					"\r\n" +
					errorContent;
			toClient.write(response.getBytes());
			toClient.flush();
		}
	}

	// Returns the MIME type based on the file extension.
	private String getMIMEtype(String fileName) {
		if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
			return "text/html";
		} else if (fileName.endsWith(".css")) {
			return "text/css";
		} else if (fileName.endsWith(".js")) {
			return "application/javascript";
		} else {
			return "application/octet-stream";
		}
	}

	@Override
	public void close() throws IOException {
	}
}
