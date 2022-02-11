package com.rtbhouse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

public class Worker {
    public static void main(String[] args) throws Exception {
        int serverPort = Integer.parseInt(args[0]);

        Worker worker = new Worker();
        worker.run(serverPort);
    }

    public void run(int serverPort) throws Exception {
        Server server = new Server(buildThreadPool());
        server.setConnectors(buildConnectors(server, serverPort));
        server.setHandler(buildHandler());
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception e) {
                System.out.println(e);
            }
        }));
    }

    private ThreadPool buildThreadPool() {
        return new QueuedThreadPool(32, 32, new BlockingArrayQueue<>(100));
    }

    private Connector[] buildConnectors(Server server, int serverPort) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(serverPort);

        return new Connector[] { connector };
    }

    private Handler buildHandler() {
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.addServlet(FibServlet.class, "/");
        return servletHandler;
    }
}
