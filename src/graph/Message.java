package graph;

import java.util.Date;

public class Message {
	// members for the message class
	public final byte[] data;
	public final String asText;
	public final double asDouble;
	public final Date date;
	
	// first constructor - gets the message as a string and fill all the members of the class
	public Message(String asText) {
		this.asText = asText;
		this.date = new Date();
		this.data = asText.getBytes();
		double tempDouble;
        try {
            tempDouble = Double.parseDouble(asText);
        } catch (NumberFormatException e) {
            tempDouble = Double.NaN;
        }
        this.asDouble = tempDouble;
		
	}
	
	// second constructor - gets the message as a bytes array and fill all the members of the class (use the first constructor)
	public Message(byte[] data) {
		// we convert the bytes array to string and then update all the members
		this(data.toString());
	}
	
	// third constructor - gets the message as a double and fill all the members of the class (use the first constructor)
	public Message(double asDouble) {
		// we convert the double to string and then update all the members
		this(Double.toString(asDouble));
	}
	
	public Message(Message other) {
		this(other.asText);
	}
	
}