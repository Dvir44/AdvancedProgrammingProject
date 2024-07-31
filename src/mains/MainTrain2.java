package mains;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import graph.ParallelAgent;

public class MainTrain2 { // just a simple tests about the parallel agent to get you going...

    static String tn = null;

    public static class TestAgent1 implements Agent {

        public void reset() {
        }

        public void close() {
        }

        public String getName() {
            return getClass().getName();
        }

        @Override
        public void callback(String topic, Message msg) {
            tn = Thread.currentThread().getName();
        }

    }

    public static class TestAgent2 implements Agent {

        public void reset() {
        }

        public void close() {
        }

        public String getName() {
            return getClass().getName();
        }

        @Override
        public void callback(String topic, Message msg) {
            System.out.println("Received message: " + msg.asText);
        }

    }

    public static void main(String[] args) {
        TopicManager tm = TopicManagerSingleton.get();
        int tc = Thread.activeCount();
        ParallelAgent pa = new ParallelAgent(new TestAgent1(), 10);
        tm.getTopic("A").subscribe(pa);

        if (Thread.activeCount() != tc + 1) {
            System.out.println("your ParallelAgent does not open a thread (-10)");
        }

        tm.getTopic("A").publish(new Message("a"));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        if (tn == null) {
            System.out.println("your ParallelAgent didn't run the wrapped agent callback (-20)");
        } else {
            if (tn.equals(Thread.currentThread().getName())) {
                System.out.println("the ParallelAgent does not run the wrapped agent in a different thread (-10)");
            }
            String last = tn;
            tm.getTopic("A").publish(new Message("a"));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            if (!last.equals(tn))
                System.out.println("all messages should be processed in the same thread of ParallelAgent (-10)");
        }

        // Additional tests
        testQueueFull();
        testReset();
        testClose();

        pa.close();

        System.out.println("done");
    }

    public static void testQueueFull() {
        TopicManager tm = TopicManagerSingleton.get();
        ParallelAgent pa = new ParallelAgent(new TestAgent2(), 2); // Small capacity to fill quickly
        tm.getTopic("B").subscribe(pa);

        tm.getTopic("B").publish(new Message("msg1"));
        tm.getTopic("B").publish(new Message("msg2"));
        tm.getTopic("B").publish(new Message("msg3")); // This should block or be handled

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        pa.close();
    }

    public static void testReset() {
        TestAgent2 agent = new TestAgent2();
        ParallelAgent pa = new ParallelAgent(agent, 10);
        agent.reset();

        // Add assertions or checks if needed

        pa.close();
    }

    public static void testClose() {
        ParallelAgent pa = new ParallelAgent(new TestAgent2(), 10);
        pa.close();

        if (!pa.isClosed()) {
            System.out.println("ParallelAgent did not close properly (-10)");
        }
    }
}
