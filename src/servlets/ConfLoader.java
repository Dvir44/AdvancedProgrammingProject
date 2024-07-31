package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        System.out.println("Creating table HTML...");
        StringBuilder html = new StringBuilder();
        html.append("<html>\n\t<body>\n")
            .append("\t\t<table border='1'>\n\t\t\t<tr><th>Topic</th><th>Message</th></tr>\n");

        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            html.append("\t\t\t<tr><td>").append(topic.name).append("</td>")
                .append("<td>").append(topic.getMessage().asText).append("</td></tr>\n");
        }

        html.append("\t\t</table>\n\t</body>\n</html>");
        String htmlContent = html.toString();
        Path tableFilePath = Paths.get(this.tablePath);

        try {
            Files.deleteIfExists(tableFilePath);
            Files.write(tableFilePath, htmlContent.getBytes());
            System.out.println("Table HTML created at " + tableFilePath.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while writing the table HTML.");
            e.printStackTrace();
        }
    }

    public void createGraph() {
        System.out.println("Creating graph HTML...");
        Graph graph = new Graph();
        graph.createFromTopics();

        if (graph.hasCycles()) {
            System.out.println("Graph has cycles. Aborting graph creation.");
            return;
        }

        List<String> graphHtmlLines = HtmlGraphWriter.getGraphHTML(graph, this.directory + "/graphTemplate.html");

        Path graphFilePath = Paths.get(this.graphPath);
        try {
            Files.deleteIfExists(graphFilePath);
            Files.write(graphFilePath, String.join("\n", graphHtmlLines).getBytes());
            System.out.println("Graph HTML created at " + graphFilePath.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while writing the graph HTML.");
            e.printStackTrace();
        }
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        if (!"POST".equals(ri.getHttpCommand())) {
            String response = "HTTP/1.1 405 Method Not Allowed\r\n\r\n";
            toClient.write(response.getBytes());
            toClient.flush();
            return;
        }

        Map<String, String> parameters = ri.getParameters();
        String fileName = parameters.get("filename");
        byte[] fileContent = ri.getContent();

        System.out.println("Received fileName: " + fileName);
        System.out.println("Received fileContent length: " + (fileContent != null ? fileContent.length : "null"));

        if (fileName == null || fileName.isEmpty() || fileContent == null || fileContent.length == 0) {
            String response = "HTTP/1.1 400 Bad Request\r\n\r\nMissing file name or content";
            toClient.write(response.getBytes());
            toClient.flush();
            System.out.println("Missing file name or content.");
            return;
        }

        Path filePath = Paths.get(directory, fileName);
        Files.write(filePath, fileContent);

        config.setConfFile(filePath.toString());
        config.create();

        Files.deleteIfExists(filePath);

        this.createGraph();
        this.createTable();

        String response = "HTTP/1.1 200 OK\r\n\r\n";
        toClient.write(response.getBytes());
        toClient.flush();
        System.out.println("Configuration uploaded and processed successfully.");
    }

    @Override
    public void close() throws IOException {
        config.close();
        Files.deleteIfExists(Paths.get(this.graphPath));
        Files.deleteIfExists(Paths.get(this.tablePath));
    }
}
