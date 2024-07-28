package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
	private String name;
    private List<Node> edges;
    private Message msg;
    
    public Node(String name) {
    	this.name = name;
    	this.edges = new ArrayList<Node>();
    }
    
    // getters and setters
    public String getName() {
    	return this.name;
    }
    
    public List<Node> getEdges() {
    	return this.edges;
    }
    
    public Message getMessage() {
    	return this.msg;
    }
    
    public void setName(String name) {
    	this.name = name;
    }
    
    public void setEdges(List<Node> edges) {
    	this.edges = new ArrayList<>(edges);
    }
    
    public void setMessage(Message msg) {
    	this.msg = new Message(msg);
    }
    
    // methods
    public void addEdge(Node edge) {
    	if (edge == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        this.edges.add(edge);
    }
    
    public boolean hasCycles() {
        Set<Node> visited = new HashSet<>();
        Set<Node> stack = new HashSet<>();
        return findCycles(this, visited, stack);
    }

    private boolean findCycles(Node currentNode, Set<Node> visited, Set<Node> stack) {
        if (stack.contains(currentNode)) {
            return true;
        }
        if (visited.contains(currentNode)) {
            return false;
        }

        visited.add(currentNode);
        stack.add(currentNode);

        for (Node neighbor : currentNode.edges) {
            if (findCycles(neighbor, visited, stack)) {
                return true;
            }
        }

        stack.remove(currentNode);
        return false;
    }
}