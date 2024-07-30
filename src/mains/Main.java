package mains;

import server.HTTPServer;
import server.MyHTTPServer;
import servlets.TopicDisplayer;
import servlets.ConfLoader;
import servlets.HtmlLoader;

public class Main {

	public static void main(String[] args) throws Exception {
		HTTPServer server=new MyHTTPServer(8080,5);
		server.addServlet("GET", "/publish", new TopicDisplayer());
		server.addServlet("POST", "/upload", new ConfLoader("./html_files"));
		server.addServlet("GET", "/app/", new HtmlLoader("./html_files"));
		System.out.println("start");
		server.start();
		System.out.println("waits");
		System.in.read();
		System.out.println("close");
		server.close();
		System.out.println("done");
	}

}
