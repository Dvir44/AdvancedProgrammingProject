package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent {
	private Agent agent;
    private BlockingQueue<Message> queue;
    private Thread t;
    private boolean interrupt; //boolean for when to stop the while
    
    public ParallelAgent(Agent agent, int capacity) {
    	this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(capacity);
        interrupt = false;
        // creating the thread
        this.t = new Thread(() -> {
        	try {
        		while (!interrupt) {
                    Message message = queue.take();
                    String topic = message.asText;
                    agent.callback(topic, message);
                }
        	}
        	catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        });
        
        t.start();
    }
    
    @Override
    public String getName() {
        return agent.getName();
    }

    @Override
    public void reset() {
        agent.reset();
    }
    
    @Override
    public void callback(String topic, Message msg) {
    	try {
    		queue.put(new Message(topic));
    		queue.put(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
        interrupt = true;
    }
}