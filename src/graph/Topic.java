package graph;

import java.util.ArrayList;
import java.util.List;

public class Topic {
	// members of the Topic class
	public final String name;
	private List<Agent> subs;
	private List<Agent> pubs;
	private Message msg;
	
	Topic(String name) {
		this.name = name;
        this.subs = new ArrayList<Agent>();
        this.pubs = new ArrayList<Agent>();
        this.msg = new Message("");
	}
	
	// getters
	public String getName() {
		return this.name;
	}
	
	public List<Agent> getSubs(){
		return subs;
	}
	
	public List<Agent> getPubs() {
		return pubs;
	}
	
	public Message getMessage() {
		return this.msg;
	}
	
	public void subscribe(Agent other) {
		if (!subs.contains(other)) {
			subs.add(other);
			return;
		}
		System.out.println("Agent already in the system");
	}
	
	public void unsubscribe(Agent other) {
		if (subs.contains(other)) {
			subs.remove(other);
			return;
		}
		System.out.println("Agent not in the system");		
	}
	
	public void publish(Message msg) {
		this.msg = msg;
		for (Agent sub : subs) {
            sub.callback(name, msg);
        }
	}
	
	public void addPublisher(Agent other) {
		if (!pubs.contains(other)) {
			pubs.add(other);
			return;
		}
		System.out.println("Agent already in the system");
	}
	
	public void removePublisher(Agent other) {
		if (pubs.contains(other)) {
			pubs.remove(other);
			return;
		}
		System.out.println("Agent not in the system");
	}
}
