package graph;

import java.util.Arrays;
import graph.TopicManagerSingleton.TopicManager;

public class PlusAgent implements Agent {
	private static int countAgents = 0;
	private String[] subs;
	private String[] pubs;
	private TopicManager manager;
	
	private String topic1;
	private String topic2;
	private String output;
	
	private double x;
	private double y;
	
	public PlusAgent(String[] subs, String[] pubs) {
		this.subs = Arrays.copyOf(subs, subs.length);
		this.pubs = Arrays.copyOf(pubs, pubs.length);
		manager = TopicManagerSingleton.get();
		
		// check if the array is not big enough
		if (subs.length < 2) {
			this.topic1 = "0";
			this.topic2 = "0";
		}
		else {
			this.topic1 = subs[0];
			this.topic2 = subs[1];
		}
		// check if the array in not big enough
		if (pubs.length < 1) {
			this.output = "0";
		}
		else {
			this.output = pubs[0];
		}
		
		// reset the values before use
		this.x = Double.NaN;
		this.y = Double.NaN;
		
		// subscribe the agent to each of the two topics and give him the opportunity to publish
		manager.getTopic(topic1).subscribe(this);
		manager.getTopic(topic2).subscribe(this);
		manager.getTopic(output).addPublisher(this);
		
		countAgents++;
	}
	
	@Override
	public String getName() {
		return "Plus Agent Number " + countAgents;
	}
	
	@Override
	public void reset() {
		this.topic1 = "0";
		this.topic2 = "0";
		this.output = "0";
		this.x = Double.NaN;
		this.y = Double.NaN;
	}
	
	@Override
	public void callback(String topic, Message msg) {
		// convert the message to double
		Double msg_double = msg.asDouble;
		if (!Double.isNaN(msg_double)) { // checks if we can't convert the message
			// checks if the first or second message are the topic we look for
			if (topic.equals(this.topic1)) {
				this.x = msg_double;
			}
			if (topic.equals(this.topic2)) {
				this.y = msg_double;
			}
			// checks if both inputs been changed before doing the operation
			if (Double.isNaN(this.x) || Double.isNaN(this.y)) {
				return;
			}
			Message resultMessage = new Message(x+y);
			manager.getTopic(this.output).publish(resultMessage); // publish it
		}
	}
	
	@Override
	public void close() {
		return;
	}




}
