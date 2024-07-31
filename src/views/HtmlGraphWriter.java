package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graph.Graph;
import graph.Node;

public class HtmlGraphWriter {

    public static List<String> getGraphHTML(Graph graph, String pathTemplate) {
        List<String> htmlContent = new ArrayList<>();

        try {
            String template = new String(Files.readAllBytes(Paths.get(pathTemplate)));

            StringBuilder nodesBuilder = new StringBuilder();
            StringBuilder edgesBuilder = new StringBuilder();

            for (Node node : graph) {
                String color = determineColor(node.getName().charAt(0));

                nodesBuilder.append(String.format("{ id: '%s', message: '%s', color: '%s' },",
                        node.getName().substring(1), node.getMessage().asText, color));

                for (Node edge : node.getEdges()) {
                    edgesBuilder.append(String.format("{ source: '%s', target: '%s' },",
                            node.getName().substring(1), edge.getName().substring(1)));
                }
            }

            if (nodesBuilder.length() > 0) {
                nodesBuilder.setLength(nodesBuilder.length() - 1);
            }
            if (edgesBuilder.length() > 0) {
                edgesBuilder.setLength(edgesBuilder.length() - 1);
            }

            String graphData = String.format("createGraph([%s], [%s]);", nodesBuilder.toString(), edgesBuilder.toString());
            String filledTemplate = template.replace("// createGraph(NODES, EDGES);", graphData);

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

    private static String determineColor(char firstChar) {
        switch (firstChar) {
            case 'T':
                return "pink";
            case 'A':
                return "green";
            default:
                return "purple";
        }
    }
}
