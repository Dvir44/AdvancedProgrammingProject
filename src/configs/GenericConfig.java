package configs;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graph.Agent;
import graph.ParallelAgent;
import graph.TopicManagerSingleton;

public class GenericConfig implements Config {
    private List<Agent> agents; // List to store agents
    private String confFile; // Path to the configuration file

    public GenericConfig() {
        agents = new ArrayList<>(); // Initialize agents list
        this.confFile = ""; // Initialize configuration file path
    }

    // Set the configuration file path and close any existing agents
    public void setConfFile(String file) {
        if (!this.confFile.equals(file)) {
            this.confFile = file;
            this.close(); // Close existing agents if the file path changes
        }
    }

    @Override
    public void create() {
        List<String> file_lines; // Lines read from the configuration file
        int capacity = 50; // Capacity for ParallelAgent
        try {
            file_lines = Files.readAllLines(Paths.get(this.confFile)); // Read all lines from the file
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error can not read this file " + this.confFile + " in config " + this.getName());
            return;
        }

        // Check if the number of lines is divisible by 3
        if (file_lines.size() % 3 != 0) {
            System.out.println("Incorrect input of the file:" + this.confFile);
            return;
        }

        // Process lines in groups of three
        for (int i = 0; i < file_lines.size(); i = i + 3) {
            String className = file_lines.get(i); // Class name of the agent
            String[] subs = file_lines.get(i + 1).split(","); // Subscription topics
            String[] pubs = file_lines.get(i + 2).split(","); // Publication topics

            try {
                // Load the class and create an instance using reflection
                Class<?> agentClass = Class.forName(className);
                Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);
                Agent agent = (Agent) constructor.newInstance((Object) subs, (Object) pubs);
                Agent parallelAgent = new ParallelAgent(agent, capacity); // Wrap in ParallelAgent

                // Add agent if not already present
                if (agents.stream().noneMatch(parallelAgent::equals)) {
                    agents.add(parallelAgent);
                } else {
                    parallelAgent.close(); // Close if already present
                }
            } catch (ClassNotFoundException e) {
                System.out.println("No class matches line " + (i + 1) + ": " + className + " in file " + this.confFile);
            } catch (NoSuchMethodException e) {
                System.out
                        .println("No suitable constructor found for class " + className + " in file " + this.confFile);
            } catch (Exception e) {
                System.out.println("Failed to instantiate class " + className + " in file " + this.confFile);
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "GenericConfig"; // Return the name of the configuration
    }

    @Override
    public int getVersion() {
        return 1; // Return the version of the configuration
    }

    @Override
    public void close() {
        for (Agent a : this.agents) { // Close all agents in the list
            a.close();
        }
        TopicManagerSingleton.get().clear(); // Clear all topics in the TopicManager
    }
}