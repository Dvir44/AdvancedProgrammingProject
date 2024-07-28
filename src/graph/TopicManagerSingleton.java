package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {
	
	// inner class
	public static class TopicManager {	
		private static final TopicManager instance = new TopicManager();
		private ConcurrentHashMap<String, Topic> map;
		
		// constructor
		private TopicManager() {
			this.map = new ConcurrentHashMap<>();
		}
		
		public Topic getTopic(String topic) {
			return map.computeIfAbsent(topic, Topic::new);
		}
		
		public Collection<Topic> getTopics() {
			return map.values();
		}
		
		public void clear() {
			map.clear();
		}
	}
	
	public static TopicManager get(){
		return TopicManager.instance;
	}

}