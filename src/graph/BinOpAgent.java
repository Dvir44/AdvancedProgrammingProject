package graph;

import java.util.function.BinaryOperator;

import graph.TopicManagerSingleton.TopicManager;


public class BinOpAgent implements Agent {
	private String name;
	private String input1;
	private Double numberInput1;
	private String input2;
	private Double numberInput2;
	private String output;
	private BinaryOperator<Double> operation;
	private TopicManager manager;
	
	public BinOpAgent(String name, String input1, String input2, String output, BinaryOperator<Double> operation) {
		this.name = name;
		this.input1 = input1;
		this.numberInput1 = Double.NaN;
		this.input2 = input2;
		this.numberInput2 = Double.NaN;
		this.output = output;
		this.operation = operation;
		
		manager = TopicManagerSingleton.get();
		// adding the topics
        manager.getTopic(this.input1).subscribe(this);
        manager.getTopic(this.input2).subscribe(this);
        // publishing the output
        manager.getTopic(this.output).addPublisher(this);
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void reset() {
		this.input1 = "0";
		this.input2 = "0";
		this.output = "0";
			
	}
	
	@Override
	public void callback(String topic, Message msg) {
		// convert the message to double
		Double numberMessage = msg.asDouble;
		if (!Double.isNaN(numberMessage)) { // checks if we can't convert the message
			// checks if the first or second message are the topic we look for
			if (topic.equals(this.input1)) {
				this.numberInput1 = numberMessage;
			}
			if (topic.equals(this.input2)) {
				this.numberInput2 = numberMessage;
			}
			// checks if both inputs been changed before doing the operation
			if (Double.isNaN(this.numberInput1) || Double.isNaN(this.numberInput2)) {
				return;
			}
			Double result = operation.apply(this.numberInput1, this.numberInput2);
			Message resultMessage = new Message(result);
			manager.getTopic(this.output).publish(resultMessage); // publish it
		}
	}
	

	@Override
	public void close() {
		// needs to stay empty
	}

}