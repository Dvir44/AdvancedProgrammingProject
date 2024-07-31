package graph;

import java.util.Arrays;

import graph.TopicManagerSingleton.TopicManager;

public class DivideAgent implements Agent {
    private static int countAgents = 0;
    private String[] subs;
    private String[] pubs;
    private TopicManager manager;

    private String topic1;
    private String topic2;
    private String output;

    private double x;
    private double y;

    public DivideAgent(String[] subs, String[] pubs) {
        this.subs = Arrays.copyOf(subs, subs.length);
        this.pubs = Arrays.copyOf(pubs, pubs.length);
        manager = TopicManagerSingleton.get();

        // Check if the array is not big enough
        if (subs.length < 2) {
            this.topic1 = "0";
            this.topic2 = "0";
        } else {
            this.topic1 = subs[0];
            this.topic2 = subs[1];
        }
        // Check if the array is not big enough
        if (pubs.length < 1) {
            this.output = "0";
        } else {
            this.output = pubs[0];
        }

        // Reset the values before use
        this.x = Double.NaN;
        this.y = Double.NaN;

        // Subscribe the agent to each of the two topics and give it the opportunity to
        // publish
        manager.getTopic(topic1).subscribe(this);
        manager.getTopic(topic2).subscribe(this);
        manager.getTopic(output).addPublisher(this);

        countAgents++;
    }

    @Override
    public String getName() {
        return "Divide Agent Number " + countAgents;
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
        // Convert the message to double
        Double msg_double = msg.asDouble;
        if (!Double.isNaN(msg_double)) { // Check if we can't convert the message
            // Check if the message belongs to the topic we are interested in
            if (topic.equals(this.topic1)) {
                this.x = msg_double;
            }
            if (topic.equals(this.topic2)) {
                this.y = msg_double;
            }
            // Check if both inputs have been changed before performing the operation
            if (Double.isNaN(this.x) || Double.isNaN(this.y)) {
                return;
            }
            // Avoid division by zero
            if (this.y == 0) {
                throw new ArithmeticException("Division by zero");
            }
            Message resultMessage = new Message(x / y);
            manager.getTopic(this.output).publish(resultMessage); // Publish the result
        }
    }

    @Override
    public void close() {
    	if (this.topic1 != null)
            manager.getTopic(this.topic1).unsubscribe(this);
		if (this.topic2 != null)
			manager.getTopic(this.topic2).unsubscribe(this);
        if (this.output != null)
            manager.getTopic(this.output).removePublisher(this);
    }
}
