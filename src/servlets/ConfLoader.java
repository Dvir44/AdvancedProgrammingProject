package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import configs.GenericConfig;
import graph.Graph;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

public class ConfLoader implements Servlet {

	private final String graphPath;
	private final String tablePath;
	private GenericConfig config;
	private final String directory;

	public ConfLoader(String htmlDirectory) {
		this.directory = htmlDirectory;
		this.graphPath = htmlDirectory + "/graph.html";
		this.tablePath = htmlDirectory + "/table.html";
		this.config = new GenericConfig();
	}

	public void createTable() {
		// Initialize StringBuilder to construct HTML content
		StringBuilder html = new StringBuilder();
		html.append("<html>\n\t<body>\n")
				.append("\t\t<table border='1'>\n\t\t\t<tr><th>Topic</th><th>Message</th></tr>\n");

		// Populate table rows with topic data
		for (Topic topic : TopicManagerSingleton.get().getTopics()) {
			html.append("\t\t\t<tr><td>").append(topic.name).append("</td>")
					.append("<td>").append(topic.getMessage().asText).append("</td></tr>\n");
		}

		html.append("\t\t</table>\n\t</body>\n</html>");

		// Convert StringBuilder to string
		String htmlContent = html.toString();

		Path tableFilePath = Paths.get(this.tablePath);

		// Delete existing file if it exists
		try {
			Files.deleteIfExists(tableFilePath);
		} catch (IOException e) {
			System.err.println("Failed to delete existing table file.");
			e.printStackTrace();
		}

		// Write new HTML content to file
		try {
			Files.write(tableFilePath, htmlContent.getBytes());
		} catch (IOException e) {
			System.err.println("An error occurred while writing the table HTML.");
			e.printStackTrace();
		}
	}

	public void createGraph() {
		Graph graph = new Graph(); // Initialize Graph instance
		graph.createFromTopics();

		// Check for cycles and return if any are found
		if (graph.hasCycles()) {
			return;
		}

		// Generate HTML representation of the graph
		String graphHtml = String.join("\n",
				HtmlGraphWriter.getGraphHTML(graph, this.directory + "/graphTemplate.html"));

		// Delete existing file if it exists
		Path graphFilePath = Paths.get(this.graphPath);
		try {
			Files.deleteIfExists(graphFilePath);
		} catch (IOException e) {
			System.err.println("Failed to delete existing graph file.");
			e.printStackTrace();
		}

		// Write new HTML content to file
		try {
			Files.write(graphFilePath, graphHtml.getBytes());
		} catch (IOException e) {
			System.err.println("An error occurred while writing the graph HTML.");
			e.printStackTrace();
		}
	}

	@Override
	public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
		if (!"POST".equals(ri.getHttpCommand())) {
			// Respond with 405 Method Not Allowed if the HTTP command is not POST
			String response = "HTTP/1.1 405 Method Not Allowed\r\n\r\n";
			toClient.write(response.getBytes());
			toClient.flush();
			return;
		}

		Map<String, String> parameters = ri.getParameters();
		String fileName = parameters.get("filename");
		byte[] fileContent = ri.getContent();

		if (fileName != null && fileContent != null && fileContent.length > 0) {
			// Save the uploaded file content to the specified file
			Files.write(Paths.get(fileName), fileContent);

			// Process the configuration from the uploaded file
			config.setConfFile(fileName);
			config.create();

			// Delete the temporary file after processing
			File file = new File(fileName);
			file.delete();

			// Generate additional content as needed
			this.createGraph();
			this.createTable();

			// Respond with 200 OK for successful processing
			String response = "HTTP/1.1 200 OK\r\n" +
					"\r\n";
			toClient.write(response.getBytes());
			toClient.flush();
		} else {
			// Respond with 400 Bad Request if file name or content is missing
			String response = "HTTP/1.1 400 Bad Request\r\n\r\nMissing file name or content";
			toClient.write(response.getBytes());
			toClient.flush();
		}
	}

	@Override
	public void close() throws IOException {
		config.close();
		// Remove existing content
		File file = new File(this.graphPath);
		if (file.exists()) {
			file.delete();
		}
		file = new File(this.tablePath);
		if (file.exists()) {
			file.delete();
		}
	}
}
