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
		// Check if the command is GET
		if (!"GET".equals(ri.getHttpCommand())) {
			// Send a response with status code 405 (Method Not Allowed) if the command is
			// not GET
			toClient.write("HTTP/1.1 405 Method Not Allowed\r\n\r\n".getBytes(StandardCharsets.UTF_8));
			toClient.flush();
			return;
		}

		// Extract parameters from the RequestInfo
		Map<String, String> parameters = ri.getParameters();
		String topicName = parameters.get("topic");
		String messageContent = parameters.get("message");

		// If both "topic" and "message" parameters are found
		if (topicName != null && messageContent != null) {
			// Get the topic and publish the message to it
			Topic topic = TopicManagerSingleton.get().getTopic(topicName);
			Message message = new Message(messageContent);
			topic.publish(message);
		}

		// Create a response with status code 200 (OK)
		String response = "HTTP/1.1 200 OK\r\n\r\n";
		// Send the response to the client
		toClient.write(response.getBytes(StandardCharsets.UTF_8));
		toClient.flush();
	}

	@Override
	public void close() throws IOException {
		TopicManagerSingleton.get().clear();
	}
}
