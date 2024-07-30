package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.RequestParser.RequestInfo;

public class TopicDisplayer implements Servlet {

	@Override
	public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
		if (!"GET".equals(ri.getHttpCommand())) {
			toClient.write("HTTP/1.1 405 Method Not Allowed\r\n\r\n".getBytes(StandardCharsets.UTF_8));
			toClient.flush();
			return;
		}

		Map<String, String> parameters = ri.getParameters();
		String topicName = parameters.get("topic");
		String messageContent = parameters.get("message");

		if (topicName != null && messageContent != null) {
			Topic topic = TopicManagerSingleton.get().getTopic(topicName);
			Message message = new Message(messageContent);
			topic.publish(message);
		}

		String response = "HTTP/1.1 200 OK\r\n\r\n";
		toClient.write(response.getBytes(StandardCharsets.UTF_8));
		toClient.flush();
	}

	@Override
	public void close() throws IOException {
		TopicManagerSingleton.get().clear();
	}
}
