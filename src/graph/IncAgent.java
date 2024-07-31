package graph;

import java.util.Arrays;

import graph.TopicManagerSingleton.TopicManager;

public class IncAgent implements Agent{
	private static int countAgents = 0;
	private String[] subs;
	private String[] pubs;
	private TopicManager manager;
	
	private String topic1;
	private String output;
	
	private double x;
	
	public IncAgent(String[] subs, String[] pubs) {
		this.subs = Arrays.copyOf(subs, subs.length);
		this.pubs = Arrays.copyOf(pubs, pubs.length);
		manager = TopicManagerSingleton.get();
		
		// check if the array is not big enough
		if (subs.length < 1) {
			this.topic1 = "0";
		}
		else {
			this.topic1 = subs[0];
		}
		// check if the array in not big enough
		if (pubs.length < 1) {
			this.output = "0";
		}
		else {
			this.output = pubs[0];
		}
		
		// reset the value before use
		this.x = Double.NaN;
		
		// subscribe the agent to each of the two topics and give him the opportunity to publish
		manager.getTopic(topic1).subscribe(this);
		manager.getTopic(output).addPublisher(this);
		
		countAgents++;
	}
	
	@Override
	public String getName() {
		return "Inc Agent Number " + countAgents;
	}
	
	@Override
	public void reset() {
		this.topic1 = "0";
		this.output = "0";
		this.x = Double.NaN;
	}
	
	@Override
	public void callback(String topic, Message msg) {
		// convert the message to double
		Double msg_double = msg.asDouble;
		if (!Double.isNaN(msg_double) && !Double.isNaN(this.x)) { // checks if we can't convert the message
			// checks if the first or second message are the topic we look for
			if (topic.equals(this.topic1)) {
				this.x = msg_double;
			}
			Message resultMessage = new Message(x+1);
			manager.getTopic(this.output).publish(resultMessage); // publish it
		}
	}
	
	@Override
	public void close() {
		if (this.topic1 != null)
            manager.getTopic(this.topic1).unsubscribe(this);
        if (this.output != null)
            manager.getTopic(this.output).removePublisher(this);
	}

}
