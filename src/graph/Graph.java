package graph;

import java.util.ArrayList;

import graph.TopicManagerSingleton.TopicManager;


public class Graph extends ArrayList<Node> {
	
	public boolean hasCycles() {
		// activate each node for checking if he has cycles
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }
	
    public void createFromTopics() {
    	TopicManager manager = TopicManagerSingleton.get();
    	
    	for (Topic topic : manager.getTopics()) {
    		Node node = new Node("T"+topic.name);
    		this.add(node);
    		
    		for (Agent agent : topic.getSubs()) {
    			Node agentNode = getNode(agent);
    			node.addEdge(agentNode);
    		}
    		
    		for (Agent agent : topic.getPubs()) {
    			Node agentNode = getNode(agent);
    			agentNode.addEdge(node);
    		}
    	}
    }
    
    private Node getNode(Agent agent) {
        for (Node node : this) 
            if (node.getName().equals("A" + agent.getName()))
                return node;
        
        Node agentNode = new Node("A" + agent.getName());
        this.add(agentNode);
        return agentNode;
    }
}