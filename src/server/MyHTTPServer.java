package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.RequestParser.RequestInfo;

import java.io.BufferedReader;


import servlets.Servlet;

public class MyHTTPServer extends Thread implements HTTPServer {
	private ConcurrentMap<String, Servlet> get;
    private ConcurrentMap<String, Servlet> post;
    private ConcurrentMap<String, Servlet> delete;
    
    private final int port;
    private final int maxThreads;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    private volatile boolean running; // for the thread
    
    public MyHTTPServer(int port, int maxThreads) throws IOException {
    	this.port = port;
    	this.maxThreads = maxThreads;
    	try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize server socket on port " + port, e);
        }
        //this.threadPool = Executors.newFixedThreadPool(maxThreads);
        this.get = new ConcurrentHashMap<String, Servlet>();
        this.post = new ConcurrentHashMap<String, Servlet>();
        this.delete = new ConcurrentHashMap<String, Servlet>();
    }
    
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
    	switch (httpCommand.toUpperCase()) {
        case "GET":
            get.put(uri, s);
            break;
        case "POST":
            post.put(uri, s);
            break;
        case "DELETE":
            delete.put(uri, s);
            break;
        default:
            throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
    }
    }
    
    @Override
	public void removeServlet(String httpCommand, String uri) {
    	switch (httpCommand.toUpperCase()) {
        case "GET":
            get.remove(uri);
            break;
        case "POST":
            post.remove(uri);
            break;
        case "DELETE":
            delete.remove(uri);
            break;
        default:
            throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
    }
    }
    
    @Override
	public void start() {
    	running = true;
        threadPool = Executors.newFixedThreadPool(maxThreads);
        new Thread(this).start();
    }
    
    @Override
	public void close() {
    	running = false;
        threadPool.shutdown();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeServlets(get);
        closeServlets(post);
        closeServlets(delete);
    }
    
    private void closeServlets(Map<String, Servlet> servlets) {
        for (Servlet servlet : servlets.values()) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void run() {
        try {
            //serverSocket = new ServerSocket(port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        } 
    }
    
    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
            RequestInfo requestInfo = RequestParser.parseRequest(in);
            String command = requestInfo.getHttpCommand();
            String uri = requestInfo.getUri();
            Servlet servlet = findServlet(command, uri);
            if (servlet != null) {
                servlet.handle(requestInfo, out);
            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Servlet findServlet(String command, String uri) {
        Map<String, Servlet> commandMap;
        switch (command.toUpperCase()) {
            case "GET":
                commandMap = get;
                break;
            case "POST":
                commandMap = post;
                break;
            case "DELETE":
                commandMap = delete;
                break;
            default:
                return null;
        }
        return commandMap.get(uri);
    }
}
