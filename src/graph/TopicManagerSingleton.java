package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {
	
	// inner class
	public static class TopicManager {	
		private static final TopicManager instance;
		private ConcurrentHashMap<String, Topic> map;
		
		// for the instance to get created only once
		static { instance = new TopicManager(); }
		
		// constructor
		private TopicManager() {
			this.map = new ConcurrentHashMap<String, Topic>();
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