package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graph.Graph;
import graph.Node;

public class HtmlGraphWriter {

    // Method to generate HTML content for the graph
    public static List<String> getGraphHTML(Graph graph, String pathTemplate) {
        List<String> htmlContent = new ArrayList<>();

        try {
            // Read the template file
            String template = new String(Files.readAllBytes(Paths.get(pathTemplate)));

            // Initialize StringBuilders for nodes and edges
            StringBuilder nodesBuilder = new StringBuilder();
            StringBuilder edgesBuilder = new StringBuilder();

            // Iterate through the nodes of the graph
            for (Node node : graph) {
                // Determine the color based on the node name
                String color = determineColor(node.getName().charAt(0));

                // Add node information to nodesBuilder
                nodesBuilder.append(String.format("{ id: '%s', message: '%s', color: '%s' },",
                        node.getName().substring(1), node.getMessage().asText, color));

                // Add edges information to edgesBuilder
                for (Node edge : node.getEdges()) {
                    edgesBuilder.append(String.format("{ source: '%s', target: '%s' },",
                            node.getName().substring(1), edge.getName().substring(1)));
                }
            }

            // Remove the trailing comma from nodesBuilder and edgesBuilder if not empty
            if (nodesBuilder.length() > 0) {
                nodesBuilder.setLength(nodesBuilder.length() - 1);
            }
            if (edgesBuilder.length() > 0) {
                edgesBuilder.setLength(edgesBuilder.length() - 1);
            }

            // Create the graph data script
            String graphData = String.format("createGraph([%s], [%s]);", nodesBuilder.toString(),
                    edgesBuilder.toString());

            // Replace the placeholder in the template with the generated graph data
            String filledTemplate = template.replace("// createGraph({{NODES}}, {{EDGES}});", graphData);

            // Split the filled template into lines and add them to htmlContent
            String[] lines = filledTemplate.split("\n");
            for (String line : lines) {
                htmlContent.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
            htmlContent.add("<p>Error generating graph visualization.</p>");
        }

        return htmlContent;
    }

    // Method to determine the color based on the node name
    private static String determineColor(char firstChar) {
        switch (firstChar) {
            case 'T':
                return "blue";
            case 'A':
                return "red";
            default:
                return "black";
        }
    }
}
