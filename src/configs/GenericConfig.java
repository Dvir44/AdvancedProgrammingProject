package configs;

import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import graph.Message;
import graph.ParallelAgent;
import graph.TopicManagerSingleton;
import graph.Agent;

public class GenericConfig implements Config {
	
	private List<Agent> agents;
	private String file_name;
	private final int capacity = 30; // set a capacity for the number of agents that can be in the parallel class
	
	public GenericConfig() {
		this.agents = new ArrayList<Agent>(); // set an array for the agents
		this.file_name = "";
	}
	
	public void setConfFile(String file_name) {
		this.file_name = file_name;
	}
	
	@Override
	public void create() {
		String[] subs;
		String[] pubs;
		List<String> file_lines = new ArrayList<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(this.file_name))){
			String line;
			while ((line = reader.readLine()) != null) {
				file_lines.add(line.trim());
			}
			
			// checking that the length is valid
			if (file_lines.size() % 3 != 0) {
				System.err.println("error: the configuration file must have a number of lines that is a multiple of 3");
				return;
			}
			System.out.println(file_lines);
			System.out.println(file_lines.get(0));
			
			// read every 3 lines now
			for (int i=0; i<file_lines.size(); i+=3) {
				// subs and pubs
				subs = file_lines.get(i + 1).split(",");
				pubs = file_lines.get(i+2).split(",");
				System.out.println(subs);
				System.out.println(i);
				
				System.out.println(pubs);
				System.out.println(i);
				// building the agent
				Class<?> class1 = Class.forName(file_lines.get(i));
                Constructor<?> constructor = class1.getConstructor(subs.getClass(), pubs.getClass());
                Agent agent = (Agent) constructor.newInstance(subs, pubs);

                Agent parallelAgent = new ParallelAgent(agent, capacity);
                agents.add(parallelAgent);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getName() {
		return "Generic Config";
	}
	
	@Override
	public int getVersion() {
		return 1;
	}
	
	@Override
	public void close() { // calls the close method of each one of the agents
		for (Agent agent : this.agents) {
			agent.close();
		}
	}
}
