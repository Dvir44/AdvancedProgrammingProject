package mains;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import graph.TopicManagerSingleton.TopicManager;
import configs.Config;
import configs.MathExampleConfig;
import graph.Node;
import graph.TopicManagerSingleton;
import graph.Graph;
import graph.Message;
import graph.Agent;
import graph.BinOpAgent;

public class MainTrain3 {

    public static boolean hasCycles(List<Node> graph) {
        for (Node node : graph) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    public static void testNode() {
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");

        if (!nodeA.getName().equals("A") || !nodeB.getName().equals("B")) {
            System.out.println("Node name getter/setter issue (-10)");
        }

        nodeA.addEdge(nodeB);
        if (nodeA.getEdges().size() != 1 || !nodeA.getEdges().contains(nodeB)) {
            System.out.println("Node edge addition issue (-10)");
        }

        // Test cycle detection
        Node nodeC = new Node("C");
        nodeB.addEdge(nodeC);
        nodeC.addEdge(nodeA); // Introduce cycle

        List<Node> graph = new ArrayList<>(Arrays.asList(nodeA, nodeB, nodeC));
        if (!hasCycles(graph)) {
            System.out.println("Cycle detection failed (-20)");
        }
    }

    public static void testBinOpAgent() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        
        BinOpAgent plusAgent = new BinOpAgent("plus", "A", "B", "R1", (x, y) -> x + y);
        BinOpAgent minusAgent = new BinOpAgent("minus", "A", "B", "R2", (x, y) -> x - y);
        BinOpAgent mulAgent = new BinOpAgent("mul", "R1", "R2", "R3", (x, y) -> x * y);
        
        GetAgent ga = new GetAgent("R3");

        Random r = new Random();
        int x = 1 + r.nextInt(100);
        int y = 1 + r.nextInt(100);
        tm.getTopic("A").publish(new Message(x));
        tm.getTopic("B").publish(new Message(y));
        double rslt = (x + y) * (x - y);

        if (Math.abs(rslt - ga.msg.asDouble) > 0.05) {
            System.out.println("BinOpAgent did not produce the desired result (-20)");
        }
    }

    public static void testGraph() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        
        Config config = new MathExampleConfig();
        config.create();
        
        Graph graph = new Graph();
        graph.createFromTopics();

        if (graph.size() != 8) {
            System.out.println("Graph size incorrect (-10)");
        }

        List<String> expectedNodes = Arrays.asList("TA", "TB", "Aplus", "Aminus", "TR1", "TR2", "Amul", "TR3");
        for (Node node : graph) {
            if (!expectedNodes.contains(node.getName())) {
                System.out.println("Graph node names incorrect (-10)");
                break;
            }
        }

        if (graph.hasCycles()) {
            System.out.println("Cycle detection failed for acyclic graph (-10)");
        }

        GetAgent ga = new GetAgent("R3");
        tm.getTopic("A").addPublisher(ga); // Introduce cycle
        graph.createFromTopics();

        if (!graph.hasCycles()) {
            System.out.println("Cycle detection failed for cyclic graph (-10)");
        }
    }

    public static void testConfig() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        Config config = new MathExampleConfig();
        config.create();

        // Ensure correct setup
        if (tm.getTopic("A") == null || tm.getTopic("B") == null || tm.getTopic("R3") == null) {
            System.out.println("Config setup issue (-10)");
        }

        GetAgent ga = new GetAgent("R3");

        Random r = new Random();
        int x = 1 + r.nextInt(100);
        int y = 1 + r.nextInt(100);
        tm.getTopic("A").publish(new Message(x));
        tm.getTopic("B").publish(new Message(y));
        double rslt = (x + y) * (x - y);

        if (Math.abs(rslt - ga.msg.asDouble) > 0.05) {
            System.out.println("Config math example produced incorrect result (-20)");
        }
    }

    public static void main(String[] args) {
        testNode();
        testCycles();
        testBinOpAgent();
        testGraph();
        testConfig();
        testTopicsGraph();
        System.out.println("done");
    }

    public static class GetAgent implements Agent {
        public Message msg;

        public GetAgent(String topic) {
            TopicManagerSingleton.get().getTopic(topic).subscribe(this);
        }

        @Override
        public String getName() {
            return "Get Agent";
        }

        @Override
        public void reset() {
        }

        @Override
        public void callback(String topic, Message msg) {
            this.msg = msg;
        }

        @Override
        public void close() {
        }
    }

    public static void testCycles() {
        Node a = new Node("A");
        Node b = new Node("B");
        Node c = new Node("C");
        Node d = new Node("D");

        a.addEdge(b);
        b.addEdge(c);
        c.addEdge(d);

        // Create a graph
        List<Node> graph = new ArrayList<>();
        graph.add(a);
        graph.add(b);
        graph.add(c);
        graph.add(d);

        // Check if the graph has cycles
        boolean hasCycles = hasCycles(graph);
        if (hasCycles)
            System.out.println("wrong answer for hasCycles when there are no cycles (-20)");

        d.addEdge(a);
        hasCycles = hasCycles(graph);
        if (!hasCycles)
            System.out.println("wrong answer for hasCycles when there is a cycle (-10)");
    }

    public static void testBinGraph() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        Config c = new MathExampleConfig();
        c.create();

        GetAgent ga = new GetAgent("R3");

        Random r = new Random();
        int x = 1 + r.nextInt(100);
        int y = 1 + r.nextInt(100);
        tm.getTopic("A").publish(new Message(x));
        tm.getTopic("B").publish(new Message(y));
        double rslt = (x + y) * (x - y);

        if (Math.abs(rslt - ga.msg.asDouble) > 0.05)
            System.out.println("your BinOpAgents did not produce the desired result (-20)");
    }

    public static void testTopicsGraph() {
        TopicManager tm = TopicManagerSingleton.get();
        tm.clear();
        Config c = new MathExampleConfig();
        c.create();
        Graph g = new Graph();
        g.createFromTopics();

        if (g.size() != 8)
            System.out.println("the graph you created from topics is not in the right size (-10)");

        List<String> l = Arrays.asList("TA", "TB", "Aplus", "Aminus", "TR1", "TR2", "Amul", "TR3");
        boolean b = true;
        for (Node n : g) {
            b &= l.contains(n.getName());
        }
        if (!b)
            System.out.println("the graph you created from topics has wrong names to Nodes (-10)");

        if (g.hasCycles())
            System.out.println("Wrong result in hasCycles for topics graph without cycles (-10)");

        GetAgent ga = new GetAgent("R3");
        tm.getTopic("A").addPublisher(ga); // cycle
        g.createFromTopics();

        if (!g.hasCycles())
            System.out.println("Wrong result in hasCycles for topics graph with a cycle (-10)");
    }
}
