package mains;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Random;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class MainTrain1 {

    private static void testMessage() {
        Message m1 = new Message("hello");
        if (!m1.asText.equals("hello"))
            System.out.println("your code has a problem (-10)");
        if (m1.asDouble == m1.asDouble) // NaN==NaN is always false
            System.out.println("your code has a problem (-10)");
        if (!m1.date.before(new Date()) && !m1.date.equals(new Date()))
            System.out.println("your code has a problem (-10)");

        Message m2 = new Message(4.5);
        if (!m2.asText.equals("4.5"))
            System.out.println("your code has a problem (-10)");
        if (m2.asDouble != 4.5)
            System.out.println("your code has a problem (-10)");
        if (!m2.date.before(new Date()) && !m2.date.equals(new Date()))
            System.out.println("your code has a problem (-10)");

        Message m3 = new Message(m2);
        if (!m3.asText.equals("4.5"))
            System.out.println("your code has a problem (-10)");
        if (m3.asDouble != 4.5)
            System.out.println("your code has a problem (-10)");
        if (!m3.date.before(new Date()) && !m3.date.equals(new Date()))
            System.out.println("your code has a problem (-10)");
    }

    static abstract class AAgent implements Agent {
        public void reset() {}
        public void close() {}
        public String getName(){
            return getClass().getName();
        }
    }

    public static class TestAgent1 extends AAgent {

        double sum=0;
        int count=0;
        TopicManager tm=TopicManagerSingleton.get();

        public TestAgent1(){
            tm.getTopic("Numbers").subscribe(this);
        }

        @Override
        public void callback(String topic, Message msg) {
            count++;
            sum+=msg.asDouble;
            
            if(count%5==0){
                tm.getTopic("Sum").publish(new Message(sum));
                count=0;
            }
        }
    }

    public static class TestAgent2 extends AAgent {

        double sum=0;
        TopicManager tm=TopicManagerSingleton.get();

        public TestAgent2(){
            tm.getTopic("Sum").subscribe(this);
        }

        @Override
        public void callback(String topic, Message msg) {
            sum=msg.asDouble;
        }

        public double getSum(){
            return sum;
        }
    }

    public static void testAgents(){        
        TopicManager tm=TopicManagerSingleton.get();
        TestAgent1 a=new TestAgent1();
        TestAgent2 a2=new TestAgent2();        
        double sum=0;
        for(int c=0;c<3;c++){
            Topic num=tm.getTopic("Numbers");
            Random r=new Random();
            for(int i=0;i<5;i++){
                int x=r.nextInt(1000);
                num.publish(new Message(x));
                sum+=x;
            }
            double result=a2.getSum();
            if(result!=sum){
                System.out.println("your code published a wrong result (-10)");
            }
        }
        a.close();
        a2.close();
    }

    public static void testTopic() {
        TopicManager tm = TopicManagerSingleton.get();
        Topic numbers = tm.getTopic("Numbers");
        Topic sum = tm.getTopic("Sum");

        if (!numbers.getName().equals("Numbers")) {
        	System.out.println(numbers.getName());
            System.out.println("your code has a problem (-10)");
        }
        if (!sum.getName().equals("Sum")) {
        	System.out.println(sum.getName());
            System.out.println("your code has a problem (-10)");
        }
        numbers.subscribe(new TestAgent1());
        sum.subscribe(new TestAgent2());

        if (numbers.getSubs().size() != 1) {
        	System.out.println(numbers.getSubs().size());
        	System.out.println(numbers.getSubs().toString());
            System.out.println("your code has a problem (-10)");
        }
        if (sum.getSubs().size() != 1) {
        	System.out.println(sum.getPubs().size());
        	System.out.println(sum.getPubs().toString());
            System.out.println("your code has a problem (-10)");

        }
    }

    public static void main(String[] args) {
        testMessage();
        testAgents();
        testTopic();
        System.out.println("done");
    }
}
